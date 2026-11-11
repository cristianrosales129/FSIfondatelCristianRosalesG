import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;

public class Orden {

    // Consulta con JOIN para mostrar nombres en vez de códigos en la tabla
    static String todos = "SELECT o.folio, o.fecha_hora, c.nombrec AS cliente, "
            + "o.mesas_numero AS mesa, o.silla, a.nombre AS alimento, "
            + "m.nombree AS mesero, k.nombree AS cocinero, "
            + "o.estadoo AS estado, o.observaciones "
            + "FROM ordenes o "
            + "JOIN clientes c ON c.codigoc = o.clientes_codigoc "
            + "JOIN alimentos a ON a.idalimentos = o.alimentos_idalimentos "
            + "JOIN empleados m ON m.codigo = o.empleados_codigo_mesero "
            + "JOIN empleados k ON k.codigo = o.empleados_codigo_cocinero "
            + "ORDER BY o.fecha_hora DESC";

    int folio, silla, mesa, idAlimento;
    // fechaHora con formato "yyyy-MM-dd HH:mm:ss"
    String fechaHora, observaciones, estado;
    String codigoCliente, codigoMesero, codigoCocinero;
    String error = "";   // último mensaje de error de la base de datos

    // folio = 0 al registrar una orden nueva: lo genera la base de datos
    public Orden(int folio, String fechaHora, String observaciones,
                 String estado, int silla, String codigoCliente, int mesa,
                 int idAlimento, String codigoMesero, String codigoCocinero) {
        this.folio = folio;
        this.fechaHora = fechaHora;
        this.observaciones = observaciones;
        this.estado = estado;
        this.silla = silla;
        this.codigoCliente = codigoCliente;
        this.mesa = mesa;
        this.idAlimento = idAlimento;
        this.codigoMesero = codigoMesero;
        this.codigoCocinero = codigoCocinero;
    }

    // INSERTAR ORDEN (después de insertar, "folio" queda con el folio generado)
    public int insertar(Connection con) {
        int resultado = 0;

        String sql = "INSERT INTO ordenes "
                + "(fecha_hora, observaciones, estadoo, silla, clientes_codigoc, "
                + "mesas_numero, alimentos_idalimentos, "
                + "empleados_codigo_mesero, empleados_codigo_cocinero) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, Timestamp.valueOf(fechaHora));
            ps.setString(2, observaciones == null ? "" : observaciones);
            ps.setString(3, estado);
            ps.setInt(4, silla);
            ps.setString(5, codigoCliente);
            ps.setInt(6, mesa);
            ps.setInt(7, idAlimento);
            ps.setString(8, codigoMesero);
            ps.setString(9, codigoCocinero);

            resultado = ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    folio = rs.getInt(1);
                }
            }

        } catch (Exception e) {
            error = e.getMessage();
            System.out.println("Error al insertar orden: " + error);
        }

        return resultado;
    }

    // BORRAR ORDEN
    public int borrar(Connection con) {
        int resultado = 0;

        String sql = "DELETE FROM ordenes WHERE folio = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, folio);

            resultado = ps.executeUpdate();

        } catch (Exception e) {
            error = e.getMessage();
            System.out.println("Error al borrar orden: " + error);
        }

        return resultado;
    }

    // ACTUALIZAR ORDEN
    public int actualizar(Connection con) {
        int resultado = 0;

        String sql = "UPDATE ordenes SET "
                + "fecha_hora = ?, observaciones = ?, estadoo = ?, silla = ?, "
                + "clientes_codigoc = ?, mesas_numero = ?, "
                + "alimentos_idalimentos = ?, "
                + "empleados_codigo_mesero = ?, empleados_codigo_cocinero = ? "
                + "WHERE folio = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(fechaHora));
            ps.setString(2, observaciones == null ? "" : observaciones);
            ps.setString(3, estado);
            ps.setInt(4, silla);
            ps.setString(5, codigoCliente);
            ps.setInt(6, mesa);
            ps.setInt(7, idAlimento);
            ps.setString(8, codigoMesero);
            ps.setString(9, codigoCocinero);
            ps.setInt(10, folio);

            resultado = ps.executeUpdate();

        } catch (Exception e) {
            error = e.getMessage();
            System.out.println("Error al actualizar orden: " + error);
        }

        return resultado;
    }
}