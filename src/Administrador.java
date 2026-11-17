import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import javax.swing.table.DefaultTableModel;

/**
 * Administración de los EMPLEADOS del restaurante (tabla empleados).
 * Mantiene la misma forma de uso que tenía con la tabla "usuarios", así que
 * la ventana FAdministrador no necesita cambios:
 *   id      -> número que se muestra en la tabla (se relaciona con empleados.codigo)
 *   usuario -> empleados.nom_usr      nip -> empleados.nip
 *   rol     -> empleados.puesto       estado -> empleados.estadoe
 */
public class Administrador {

    static String todos = "SELECT codigo, nombree, nom_usr, puesto, estadoe "
            + "FROM empleados ORDER BY codigo";
    static String buscar = "SELECT codigo, nombree, nom_usr, puesto, estadoe "
            + "FROM empleados WHERE nombree LIKE ? OR nom_usr LIKE ? OR codigo LIKE ? "
            + "ORDER BY codigo";

    // empleados.codigo es texto (MES001, COC002...), la ventana maneja un número.
    // Estos mapas relacionan cada código con su número (que no cambia entre consultas).
    private static final Map<String, Integer> idDeCodigo = new HashMap<>();
    private static final Map<Integer, String> codigoDeId = new HashMap<>();

    private static int idPara(String codigo) {
        Integer id = idDeCodigo.get(codigo);
        if (id == null) {
            id = idDeCodigo.size() + 1;
            idDeCodigo.put(codigo, id);
            codigoDeId.put(id, codigo);
        }
        return id;
    }

    // En la ventana el rol se llama "Administrador"; en la tabla es el puesto "gerente"
    private static String puestoAMostrar(String puesto) {
        if (puesto == null) return "";
        if (puesto.equalsIgnoreCase("gerente")) return "Administrador";
        return puesto.isEmpty() ? puesto : puesto.substring(0, 1).toUpperCase() + puesto.substring(1);
    }

    private static String puestoAGuardar(String rol) {
        String p = rol.trim().toLowerCase();
        return p.equals("administrador") ? "gerente" : p;
    }

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

    // ¿Ya existe un empleado con ese código o con ese nombre de usuario? (para REGISTRAR)
    private boolean existeEmpleado(Connection con, String codigo) {
        String sql = "SELECT COUNT(*) FROM empleados WHERE codigo = ? OR nom_usr = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo);
            ps.setString(2, usuario);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            error = e.getMessage();
            System.out.println("Error al validar empleado: " + error);
        }

        return false;
    }

    // ¿Otro empleado ya usa ese nombre de usuario? (para MODIFICAR)
    private boolean existeOtroUsuario(Connection con, String codigo) {
        String sql = "SELECT COUNT(*) FROM empleados WHERE nom_usr = ? AND codigo <> ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, codigo);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            error = e.getMessage();
            System.out.println("Error al validar usuario: " + error);
        }

        return false;
    }

    // INSERTAR EMPLEADO (su código será igual a su usuario, como en tus datos)
    public int insertar(Connection con) {
        int resultado = 0;

        if (usuario.length() > 20 || nip.length() > 20) {
            error = "El usuario y el NIP deben tener máximo 20 caracteres.";
            return 0;
        }

        String codigo = usuario;
        if (existeEmpleado(con, codigo)) {
            error = "Ya existe un empleado con ese usuario.";
            return 0;
        }

        String puesto = puestoAGuardar(rol);
        String turno = puesto.equals("gerente") ? "completo" : "matutino";

        String sql = "INSERT INTO empleados "
                + "(codigo, nombree, puesto, turno, nom_usr, nip, estadoe) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo);
            ps.setString(2, nombre);
            ps.setString(3, puesto);
            ps.setString(4, turno);
            ps.setString(5, usuario);
            ps.setString(6, nip);
            ps.setString(7, estado.toLowerCase());

            resultado = ps.executeUpdate();

        } catch (Exception e) {
            error = e.getMessage();
            System.out.println("Error al insertar empleado: " + error);
        }

        return resultado;
    }

    // ACTUALIZAR EMPLEADO (si el nip viene vacío, se conserva el que ya tenía)
    public int actualizar(Connection con) {
        int resultado = 0;

        String codigo = codigoDeId.get(id);
        if (codigo == null) {
            error = "Selecciona de nuevo al empleado en la tabla.";
            return 0;
        }

        if (usuario.length() > 20 || (nip != null && nip.length() > 20)) {
            error = "El usuario y el NIP deben tener máximo 20 caracteres.";
            return 0;
        }

        if (existeOtroUsuario(con, codigo)) {
            error = "Ya existe otro empleado con ese usuario.";
            return 0;
        }

        boolean cambiaNip = nip != null && !nip.isEmpty();

        String sql = cambiaNip
                ? "UPDATE empleados SET nombree = ?, nom_usr = ?, puesto = ?, "
                  + "estadoe = ?, nip = ? WHERE codigo = ?"
                : "UPDATE empleados SET nombree = ?, nom_usr = ?, puesto = ?, "
                  + "estadoe = ? WHERE codigo = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, usuario);
            ps.setString(3, puestoAGuardar(rol));
            ps.setString(4, estado.toLowerCase());

            if (cambiaNip) {
                ps.setString(5, nip);
                ps.setString(6, codigo);
            } else {
                ps.setString(5, codigo);
            }

            resultado = ps.executeUpdate();

        } catch (Exception e) {
            error = e.getMessage();
            System.out.println("Error al actualizar empleado: " + error);
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
                ps.setString(3, like);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String estadoe = rs.getString("estadoe");
                    modelo.addRow(new Object[]{
                        idPara(rs.getString("codigo")),
                        rs.getString("nombree"),
                        rs.getString("nom_usr"),
                        puestoAMostrar(rs.getString("puesto")),
                        estadoe == null || estadoe.isEmpty() ? ""
                                : estadoe.substring(0, 1).toUpperCase() + estadoe.substring(1)
                    });
                }
            }

        } catch (Exception e) {
            System.out.println("Error al consultar empleados: " + e.getMessage());
        }

        return modelo;
    }
}