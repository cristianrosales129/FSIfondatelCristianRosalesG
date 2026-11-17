import com.mysql.jdbc.PreparedStatement;
import java.io.Serializable;
import javax.swing.ImageIcon;

public class Alimento implements Serializable {

    private int id;
    private String nombre, descripcion, categoria, estado;
    private float precio;
    private ImageIcon foto;

    public Alimento(int id, String nombre, String descripcion, float precio, ImageIcon foto, String categoria, String estado) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.foto = foto;
        this.categoria = categoria;
        this.estado = estado;
    }

    public int insertar(Conexion cnx) {
        try {
            String sql = "INSERT INTO alimentos VALUES(?,?,?,?,?,?,?)";
            PreparedStatement ps = (PreparedStatement) cnx.con.prepareStatement(sql);
            ps.setInt(1, id);
            ps.setString(2, nombre);
            ps.setString(3, descripcion);
            ps.setFloat(4, precio);
            ps.setBinaryStream(5, cnx.ImagenBytes(foto));
            ps.setString(6, categoria);
            ps.setString(7, estado);
            return ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error al insertar: " + e.getMessage());
            return 0;
        }
    }

    public int actualizar(Conexion cnx) {
        try {
            String sql = "UPDATE alimentos SET nombre=?, descripcion=?, precio=?, foto=?, categoria=?, estado=? WHERE idalimentos=?";
            PreparedStatement ps = (PreparedStatement) cnx.con.prepareStatement(sql);
            ps.setString(1, nombre);
            ps.setString(2, descripcion);
            ps.setFloat(3, precio);
            ps.setBinaryStream(4, cnx.ImagenBytes(foto));
            ps.setString(5, categoria);
            ps.setString(6, estado);
            ps.setInt(7, id);
            return ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error al actualizar: " + e.getMessage());
            return 0;
        }
    }
}
