import java.sql.Connection;
import java.sql.PreparedStatement;

public class Empleado {

    static String todos = "SELECT * FROM empleados ORDER BY nombree";
    static String codigos = "SELECT codigo FROM empleados ORDER BY codigo";
    // Para llenar los combos de mesero y cocinero en las órdenes
    static String meseros = "SELECT codigo, nombree FROM empleados "
            + "WHERE puesto = 'Mesero' ORDER BY nombree";
    static String cocineros = "SELECT codigo, nombree FROM empleados "
            + "WHERE puesto = 'Cocinero' ORDER BY nombree";
    String codigo, nombre, puesto, turno, estado;
    String error = "";   // último mensaje de error de la base de datos

    public Empleado(String codigo, String nombre, String puesto,
                    String turno, String estado) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.puesto = puesto;
        this.turno = turno;
        this.estado = estado;
    }

    // INSERTAR EMPLEADO
    public int insertar(Connection con) {
        int resultado = 0;

        String sql = "INSERT INTO empleados "
                + "(codigo, nombree, puesto, turno, estadoe) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo);
            ps.setString(2, nombre);
            ps.setString(3, puesto);
            ps.setString(4, turno);
            ps.setString(5, estado);

            resultado = ps.executeUpdate();

        } catch (Exception e) {
            error = e.getMessage();
            System.out.println("Error al insertar empleado: " + error);
        }

        return resultado;
    }

    // BORRAR EMPLEADO (falla si ya atendió o cocinó alguna orden)
    public int borrar(Connection con) {
        int resultado = 0;

        String sql = "DELETE FROM empleados WHERE codigo = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo);

            resultado = ps.executeUpdate();

        } catch (Exception e) {
            error = e.getMessage();
            System.out.println("Error al borrar empleado: " + error);
        }

        return resultado;
    }

    // ACTUALIZAR EMPLEADO
    public int actualizar(Connection con) {
        int resultado = 0;

        String sql = "UPDATE empleados SET "
                + "nombree = ?, puesto = ?, turno = ?, estadoe = ? "
                + "WHERE codigo = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, puesto);
            ps.setString(3, turno);
            ps.setString(4, estado);
            ps.setString(5, codigo);

            resultado = ps.executeUpdate();

        } catch (Exception e) {
            error = e.getMessage();
            System.out.println("Error al actualizar empleado: " + error);
        }

        return resultado;
    }
}