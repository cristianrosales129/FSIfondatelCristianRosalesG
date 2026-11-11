import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;

public class Cliente {

    static String todos = "SELECT * FROM clientes ORDER BY nombrec";
    static String codigos = "SELECT codigoc, nombrec FROM clientes ORDER BY nombrec";
    String codigo, nombre, rfc;
    String error = "";   // último mensaje de error de la base de datos

    public Cliente(String codigo, String nombre, String rfc) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.rfc = rfc;
    }

    // El RFC es opcional: si viene vacío se guarda NULL
    private void asignarRfc(PreparedStatement ps, int posicion) throws Exception {
        if (rfc == null || rfc.trim().isEmpty()) {
            ps.setNull(posicion, Types.VARCHAR);
        } else {
            ps.setString(posicion, rfc.trim());
        }
    }

    // INSERTAR CLIENTE
    public int insertar(Connection con) {
        int resultado = 0;

        String sql = "INSERT INTO clientes (codigoc, nombrec, rfc) "
                + "VALUES (?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo);
            ps.setString(2, nombre);
            asignarRfc(ps, 3);

            resultado = ps.executeUpdate();

        } catch (Exception e) {
            error = e.getMessage();
            System.out.println("Error al insertar cliente: " + error);
        }

        return resultado;
    }

    // BORRAR CLIENTE (falla si el cliente ya tiene órdenes)
    public int borrar(Connection con) {
        int resultado = 0;

        String sql = "DELETE FROM clientes WHERE codigoc = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo);

            resultado = ps.executeUpdate();

        } catch (Exception e) {
            error = e.getMessage();
            System.out.println("Error al borrar cliente: " + error);
        }

        return resultado;
    }

    // ACTUALIZAR CLIENTE
    public int actualizar(Connection con) {
        int resultado = 0;

        String sql = "UPDATE clientes SET nombrec = ?, rfc = ? "
                + "WHERE codigoc = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            asignarRfc(ps, 2);
            ps.setString(3, codigo);

            resultado = ps.executeUpdate();

        } catch (Exception e) {
            error = e.getMessage();
            System.out.println("Error al actualizar cliente: " + error);
        }

        return resultado;
    }
}