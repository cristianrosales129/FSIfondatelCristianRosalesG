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
}