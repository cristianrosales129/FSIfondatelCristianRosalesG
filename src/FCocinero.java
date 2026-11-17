import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.Statement;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Ventana del COCINERO.
 * Consulta la vista "comandas": órdenes de hoy que están en estado "ordenado".
 */
public class FCocinero extends JFrame {

    private final Conexion cnx = new Conexion();
    private final String usuario;
    private final JLabel lblInfo = new JLabel(" ");
    private final JTable tabla = new JTable(new DefaultTableModel()) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    public FCocinero() {
        this("");
    }

    public FCocinero(String usr) {
        this.usuario = usr;
        setTitle("Fondatel - Cocina");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(950, 480);
        setLocationRelativeTo(null);

        JLabel titulo = new JLabel("  Cocina - comandas pendientes de hoy" + (usr.isEmpty() ? "" : "   (usuario: " + usr + ")"));
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));
        titulo.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 5, 10, 5));

        tabla.setRowHeight(26);
        tabla.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        JButton bActualizar = new JButton("Actualizar");
        JButton bPreparado = new JButton("Marcar como preparado");
        JButton bSalir = new JButton("Cerrar sesión");
        bActualizar.addActionListener(e -> cargar());
        bPreparado.addActionListener(e -> marcarPreparado());
        bSalir.addActionListener(e -> cerrarSesion());

        JPanel sur = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sur.add(bActualizar);
        sur.add(bPreparado);
        sur.add(bSalir);
        sur.add(lblInfo);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(titulo, BorderLayout.NORTH);
        getContentPane().add(new JScrollPane(tabla), BorderLayout.CENTER);
        getContentPane().add(sur, BorderLayout.SOUTH);

        if (cnx.conectar("localhost", "fondatel", "root", "") == 1) {
            cargar();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo conectar con la base de datos");
        }
    }

    // Consulta a la vista del cocinero
    private void cargar() {
        cnx.entablar("SELECT folio, fecha_hora, mesas_numero, silla, nombre, descripcion, observaciones "
                + "FROM comandas ORDER BY fecha_hora", tabla);
        encabezados("Folio", "Hora", "Mesa", "Silla", "Platillo", "Descripción", "Observaciones");
        lblInfo.setText("   " + tabla.getRowCount() + " comanda(s) pendiente(s)");
    }

    private void encabezados(String... nombres) {
        for (int i = 0; i < nombres.length && i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setHeaderValue(nombres[i]);
        }
        tabla.getTableHeader().repaint();
    }

    private void marcarPreparado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una comanda de la tabla");
            return;
        }
        int folio = Integer.parseInt(tabla.getValueAt(fila, 0).toString());
        String sql = "UPDATE ordenes SET estadoo = 'preparado'";
        if (!usuario.isEmpty()) { // queda registrado qué cocinero la preparó
            sql += ", empleados_codigo_cocinero = (SELECT codigo FROM empleados WHERE nom_usr = '"
                    + usuario.replace("\\", "\\\\").replace("'", "''") + "')";
        }
        sql += " WHERE folio = " + folio;
        try (Statement st = cnx.getConexion().createStatement()) {
            st.executeUpdate(sql);
            cargar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo actualizar la comanda: " + ex.getMessage());
        }
    }

    private void cerrarSesion() {
        cnx.desconectar();
        dispose();
        new FAceso().setVisible(true);
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new FCocinero().setVisible(true));
    }
}
