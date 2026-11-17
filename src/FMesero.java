import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.Statement;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Ventana del MESERO.
 * Consulta la vista "pedidos" filtrada por el mesero que inició sesión.
 */
public class FMesero extends JFrame {

    private final Conexion cnx = new Conexion();
    private final String usuario;
    private String codigo = "";   // código del empleado (empleados.codigo)
    private final JLabel lblInfo = new JLabel(" ");
    private final JComboBox<String> cmbEstado = new JComboBox<>(new String[]{
        "Todos", "ordenado", "preparado", "entregado", "pagado", "devuelto", "cancelado"});
    private final JTable tabla = new JTable(new DefaultTableModel()) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    public FMesero() {
        this("");
    }

    public FMesero(String usr) {
        this.usuario = usr;
        setTitle("Fondatel - Mesero");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 520);
        setLocationRelativeTo(null);

        JLabel titulo = new JLabel("  Mis pedidos" + (usr.isEmpty() ? "" : "   (usuario: " + usr + ")"));
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));
        titulo.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 5, 5, 5));

        JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtro.add(new JLabel("Estado:"));
        filtro.add(cmbEstado);
        cmbEstado.addActionListener(e -> cargar());

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(titulo, BorderLayout.NORTH);
        norte.add(filtro, BorderLayout.SOUTH);

        tabla.setRowHeight(26);
        tabla.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        JButton bActualizar = new JButton("Actualizar");
        JButton bEntregado = new JButton("Marcar como entregado");
        JButton bSalir = new JButton("Cerrar sesión");
        bActualizar.addActionListener(e -> cargar());
        bEntregado.addActionListener(e -> marcarEntregado());
        bSalir.addActionListener(e -> cerrarSesion());

        JPanel sur = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sur.add(bActualizar);
        sur.add(bEntregado);
        sur.add(bSalir);
        sur.add(lblInfo);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(norte, BorderLayout.NORTH);
        getContentPane().add(new JScrollPane(tabla), BorderLayout.CENTER);
        getContentPane().add(sur, BorderLayout.SOUTH);

        if (cnx.conectar("localhost", "fondatel", "root", "") == 1) {
            codigo = cnx.obtenerDato("SELECT codigo FROM empleados WHERE nom_usr = '" + esc(usuario) + "'");
            cargar();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo conectar con la base de datos");
        }
    }

    private static String esc(String s) {
        return s.replace("\\", "\\\\").replace("'", "''");
    }

    // Consulta a la vista del mesero (solo sus pedidos, los más recientes primero)
    private void cargar() {
        String sql = "SELECT folio, fecha_hora, mesas_numero, silla, nombrec, nombre, precio, observaciones, estadoo "
                + "FROM pedidos WHERE empleados_codigo_mesero = '" + esc(codigo) + "'";
        String filtro = (String) cmbEstado.getSelectedItem();
        if (!"Todos".equals(filtro)) {
            sql += " AND estadoo = '" + filtro + "'";
        }
        sql += " ORDER BY fecha_hora DESC";
        cnx.entablar(sql, tabla);
        encabezados("Folio", "Fecha y hora", "Mesa", "Silla", "Cliente", "Alimento", "Precio", "Observaciones", "Estado");
        lblInfo.setText("   " + tabla.getRowCount() + " pedido(s)");
    }

    private void encabezados(String... nombres) {
        for (int i = 0; i < nombres.length && i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setHeaderValue(nombres[i]);
        }
        tabla.getTableHeader().repaint();
    }

    private void marcarEntregado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un pedido de la tabla");
            return;
        }
        String estado = String.valueOf(tabla.getValueAt(fila, 8));
        if (!"preparado".equals(estado)) {
            JOptionPane.showMessageDialog(this, "Solo se pueden entregar los pedidos en estado 'preparado'");
            return;
        }
        int folio = Integer.parseInt(tabla.getValueAt(fila, 0).toString());
        try (Statement st = cnx.getConexion().createStatement()) {
            st.executeUpdate("UPDATE ordenes SET estadoo = 'entregado' WHERE folio = " + folio);
            cargar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo actualizar el pedido: " + ex.getMessage());
        }
    }

    private void cerrarSesion() {
        cnx.desconectar();
        dispose();
        new FAceso().setVisible(true);
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new FMesero().setVisible(true));
    }
}
