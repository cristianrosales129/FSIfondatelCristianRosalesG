import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.table.DefaultTableModel;

public class Administrador {

    static String todos = "SELECT idusuarios, nombre, nom_user, rol, estado "
            + "FROM usuarios ORDER BY idusuarios";
    static String buscar = "SELECT idusuarios, nombre, nom_user, rol, estado "
            + "FROM usuarios WHERE nombre LIKE ? OR nom_user LIKE ? "
            + "ORDER BY idusuarios";

    int id;
    String nombre, usuario, nip, rol, estado;
    String error = "";   // último mensaje de error de la base de datos

    public Administrador(int id, String nombre, String usuario,
                         String nip, String rol, String estado) {
        this.id = id;
        this.nombre = nombre;
        this.usuario = usuario;
        this.nip = nip;
        this.rol = rol;
        this.estado = estado;
    }

    // ¿Ya existe otro registro con ese nombre de usuario?
    public boolean existeUsuario(Connection con) {
        String sql = "SELECT COUNT(*) FROM usuarios "
                + "WHERE nom_user = ? AND idusuarios <> ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setInt(2, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            error = e.getMessage();
            System.out.println("Error al validar usuario: " + error);
        }

        return false;
    }

    // INSERTAR USUARIO
    public int insertar(Connection con) {
        int resultado = 0;

        if (existeUsuario(con)) {
            error = "Ya existe un usuario con ese nombre de usuario.";
            return 0;
        }

        String sql = "INSERT INTO usuarios "
                + "(nombre, nom_user, nip, rol, estado) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, usuario);
            ps.setString(3, nip);
            ps.setString(4, rol);
            ps.setString(5, estado);

            resultado = ps.executeUpdate();

        } catch (Exception e) {
            error = e.getMessage();
            System.out.println("Error al insertar usuario: " + error);
        }

        return resultado;
    }

    // ACTUALIZAR USUARIO (si el nip viene vacío, se conserva el que ya tenía)
    public int actualizar(Connection con) {
        int resultado = 0;

        if (existeUsuario(con)) {
            error = "Ya existe otro usuario con ese nombre de usuario.";
            return 0;
        }

        boolean cambiaNip = nip != null && !nip.isEmpty();

        String sql = cambiaNip
                ? "UPDATE usuarios SET nombre = ?, nom_user = ?, rol = ?, "
                  + "estado = ?, nip = ? WHERE idusuarios = ?"
                : "UPDATE usuarios SET nombre = ?, nom_user = ?, rol = ?, "
                  + "estado = ? WHERE idusuarios = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, usuario);
            ps.setString(3, rol);
            ps.setString(4, estado);

            if (cambiaNip) {
                ps.setString(5, nip);
                ps.setInt(6, id);
            } else {
                ps.setInt(5, id);
            }

            resultado = ps.executeUpdate();

        } catch (Exception e) {
            error = e.getMessage();
            System.out.println("Error al actualizar usuario: " + error);
        }

        return resultado;
    }

    // CONSULTAR: devuelve el modelo para la JTable (sin mostrar el nip)
    public static DefaultTableModel consultar(Connection con, String filtro) {
        DefaultTableModel modelo = new DefaultTableModel(
                new String[]{"ID", "Nombre", "Usuario", "Rol", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        boolean hayFiltro = filtro != null && !filtro.trim().isEmpty();

        try (PreparedStatement ps = con.prepareStatement(hayFiltro ? buscar : todos)) {
            if (hayFiltro) {
                String like = "%" + filtro.trim() + "%";
                ps.setString(1, like);
                ps.setString(2, like);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    modelo.addRow(new Object[]{
                        rs.getInt("idusuarios"),
                        rs.getString("nombre"),
                        rs.getString("nom_user"),
                        rs.getString("rol"),
                        rs.getString("estado")
                    });
                }
            }

        } catch (Exception e) {
            System.out.println("Error al consultar usuarios: " + e.getMessage());
        }

        return modelo;
    }
}
