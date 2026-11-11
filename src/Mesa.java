import java.sql.Connection;
import java.sql.PreparedStatement;

public class Mesa {

    static String todos = "SELECT * FROM mesas ORDER BY numero";
    static String numeros = "SELECT numero FROM mesas ORDER BY numero";
    int numero, capacidad;
    String estado;
    String error = "";   // último mensaje de error de la base de datos

    public Mesa(int numero, String estado, int capacidad) {
        this.numero = numero;
        this.estado = estado;
        this.capacidad = capacidad;
    }

    // INSERTAR MESA
    public int insertar(Connection con) {
        int resultado = 0;

        String sql = "INSERT INTO mesas (numero, estadoM, capacidad) "
                + "VALUES (?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, numero);
            ps.setString(2, estado);
            ps.setInt(3, capacidad);

            resultado = ps.executeUpdate();

        } catch (Exception e) {
            error = e.getMessage();
            System.out.println("Error al insertar mesa: " + error);
        }

        return resultado;
    }

    // BORRAR MESA (falla si la mesa ya tiene órdenes)
    public int borrar(Connection con) {
        int resultado = 0;

        String sql = "DELETE FROM mesas WHERE numero = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, numero);

            resultado = ps.executeUpdate();

        } catch (Exception e) {
            error = e.getMessage();
            System.out.println("Error al borrar mesa: " + error);
        }

        return resultado;
    }

    // ACTUALIZAR MESA
    public int actualizar(Connection con) {
        int resultado = 0;

        String sql = "UPDATE mesas SET estadoM = ?, capacidad = ? "
                + "WHERE numero = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, capacidad);
            ps.setInt(3, numero);

            resultado = ps.executeUpdate();

        } catch (Exception e) {
            error = e.getMessage();
            System.out.println("Error al actualizar mesa: " + error);
        }

        return resultado;
    }
}