import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.Statement;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Ventana del CAJERO.
 * Consulta la vista "tickets" (órdenes entregadas, pendientes de cobro):
 * arriba la cuenta por mesa y silla, abajo el detalle de la mesa seleccionada.
 */
public class FCajero extends JFrame {

    private final Conexion cnx = new Conexion();
    private final JLabel lblTotal = new JLabel(" ");
    private final JLabel lblInfo = new JLabel(" ");
    private final JTable tabla = crearTabla();
    private final JTable detalle = crearTabla();

    private static JTable crearTabla() {
        JTable t = new JTable(new DefaultTableModel()) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        t.setRowHeight(26);
        t.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        return t;
    }

    public FCajero() {
        this("");
    }

    public FCajero(String usr) {
        setTitle("Fondatel - Caja");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        JLabel titulo = new JLabel("  Caja - cuentas por cobrar" + (usr.isEmpty() ? "" : "   (usuario: " + usr + ")"));
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));
        titulo.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        JScrollPane spCuentas = new JScrollPane(tabla);
        spCuentas.setBorder(BorderFactory.createTitledBorder("Cuenta por mesa y silla"));
        JScrollPane spDetalle = new JScrollPane(detalle);
        spDetalle.setBorder(BorderFactory.createTitledBorder("Detalle de la mesa seleccionada"));

        JPanel centro = new JPanel(new GridLayout(2, 1, 0, 6));
        centro.add(spCuentas);
        centro.add(spDetalle);

        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 14f));
        JButton bActualizar = new JButton("Actualizar");
        JButton bCobrar = new JButton("Cobrar mesa");
        JButton bSalir = new JButton("Cerrar sesión");
        bActualizar.addActionListener(e -> cargar());
        bCobrar.addActionListener(e -> cobrarMesa());
        bSalir.addActionListener(e -> cerrarSesion());

        JPanel sur = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sur.add(bActualizar);
        sur.add(bCobrar);
        sur.add(bSalir);
        sur.add(lblTotal);
        sur.add(lblInfo);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(titulo, BorderLayout.NORTH);
        getContentPane().add(centro, BorderLayout.CENTER);
        getContentPane().add(sur, BorderLayout.SOUTH);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (!e.getValueIsAdjusting() && fila >= 0) {
                mostrarDetalle(mesaSeleccionada());
            }
        });

        if (cnx.conectar("localhost", "fondatel", "root", "") == 1) {
            cargar();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo conectar con la base de datos");
        }
    }

    private int mesaSeleccionada() {
        int fila = tabla.getSelectedRow();
        return fila < 0 ? -1 : Integer.parseInt(tabla.getValueAt(fila, 0).toString());
    }

    // Consulta a la vista del cajero: subtotal por mesa y silla
    private void cargar() {
        cnx.entablar("SELECT mesas_numero, silla, COUNT(*), SUM(precio) FROM tickets "
                + "GROUP BY mesas_numero, silla ORDER BY mesas_numero, silla", tabla);
        encabezados(tabla, "Mesa", "Silla", "Platillos", "Subtotal");
        ((DefaultTableModel) detalle.getModel()).setRowCount(0);
        lblTotal.setText(" ");
        lblInfo.setText("   " + tabla.getRowCount() + " silla(s) con cuenta abierta");
    }

    private void mostrarDetalle(int mesa) {
        cnx.entablar("SELECT folio, silla, nombre, precio FROM tickets WHERE mesas_numero = " + mesa
                + " ORDER BY silla, folio", detalle);
        encabezados(detalle, "Folio", "Silla", "Alimento", "Precio");
        String total = cnx.obtenerDato("SELECT SUM(precio) FROM tickets WHERE mesas_numero = " + mesa);
        double t = (total == null || total.isEmpty()) ? 0 : Double.parseDouble(total);
        lblTotal.setText(String.format("   Total de la mesa %d: $%.2f", mesa, t));
    }

    private void encabezados(JTable t, String... nombres) {
        for (int i = 0; i < nombres.length && i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setHeaderValue(nombres[i]);
        }
        t.getTableHeader().repaint();
    }

    private void cobrarMesa() {
        int mesa = mesaSeleccionada();
        if (mesa < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una mesa en la tabla de cuentas");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "¿Cobrar toda la cuenta de la mesa " + mesa + "?",
                "Cobrar mesa", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        try (Statement st = cnx.getConexion().createStatement()) {
            st.executeUpdate("UPDATE ordenes SET estadoo = 'pagado' "
                    + "WHERE mesas_numero = " + mesa + " AND estadoo = 'entregado'");
            cargar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cobrar la mesa: " + ex.getMessage());
        }
    }

    private void cerrarSesion() {
        cnx.desconectar();
        dispose();
        new FAceso().setVisible(true);
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new FCajero().setVisible(true));
    }
}
