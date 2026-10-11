package JdbcDAO;

import config.DataBaseConnection;
import DAO.EstudianteDAO;
import modelo.Estudiante;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class JdbcEstudianteDAO implements EstudianteDAO {
    private final DataBaseConnection database;
    public JdbcEstudianteDAO(DataBaseConnection database) { this.database = database; }

    @Override public List<Estudiante> listar() throws SQLException {
        List<Estudiante> result = new ArrayList<>();
        try (var c = database.getConnection(); var s = c.prepareStatement(
                "SELECT id,nombre,rut,curso,correo FROM estudiantes ORDER BY nombre"); var rs = s.executeQuery()) {
            while (rs.next()) result.add(new Estudiante(rs.getInt("id"), rs.getString("nombre"),
                    rs.getString("rut"), rs.getString("curso"), rs.getString("correo")));
        }
        return result;
    }

    @Override public void crear(Estudiante e, String contrasenaInicial) throws SQLException {
        String sqlStudent = "INSERT INTO estudiantes(nombre,rut,curso,correo) VALUES (?,?,?,?)";
        String sqlUser = "INSERT INTO usuarios(nombre,rut,correo,contrasena,rol) VALUES (?,?,?,?,'ESTUDIANTE')";
        try (var c = database.getConnection()) {
            c.setAutoCommit(false);
            try (var student = c.prepareStatement(sqlStudent); var user = c.prepareStatement(sqlUser)) {
                fill(student, e); student.executeUpdate();
                user.setString(1, e.nombre().trim()); user.setString(2, e.rut().trim());
                user.setString(3, e.correo().trim()); user.setString(4, sha256(contrasenaInicial)); user.executeUpdate();
                c.commit();
            } catch (SQLException ex) {
                rollback(c, ex); throw ex;
            }
        }
    }

    @Override public void actualizar(Estudiante e, String rutAnterior) throws SQLException {
        String sqlStudent = "UPDATE estudiantes SET nombre=?,rut=?,curso=?,correo=? WHERE id=?";
        String sqlUser = "UPDATE usuarios SET nombre=?,rut=?,correo=? WHERE rut=? AND rol='ESTUDIANTE'";
        try (var c = database.getConnection()) {
            c.setAutoCommit(false);
            try (var student = c.prepareStatement(sqlStudent); var user = c.prepareStatement(sqlUser)) {
                fill(student, e); student.setInt(5, e.id()); student.executeUpdate();
                user.setString(1, e.nombre().trim()); user.setString(2, e.rut().trim());
                user.setString(3, e.correo().trim()); user.setString(4, rutAnterior); user.executeUpdate();
                c.commit();
            } catch (SQLException ex) {
                rollback(c, ex); throw ex;
            }
        }
    }

    private static void fill(java.sql.PreparedStatement s, Estudiante e) throws SQLException {
        s.setString(1, e.nombre().trim()); s.setString(2, e.rut().trim());
        s.setString(3, e.curso().trim()); s.setString(4, e.correo().trim());
    }

    @Override public void eliminar(int id, String rut) throws SQLException {
        try (var c = database.getConnection()) {
            c.setAutoCommit(false);
            try (var user = c.prepareStatement("DELETE FROM usuarios WHERE rut=? AND rol='ESTUDIANTE'");
                 var student = c.prepareStatement("DELETE FROM estudiantes WHERE id=?")) {
                user.setString(1, rut); user.executeUpdate();
                student.setInt(1, id); student.executeUpdate();
                c.commit();
            } catch (SQLException ex) {
                rollback(c, ex); throw ex;
            }
        }
    }

    private static String sha256(String value) {
        try {
            byte[] bytes = java.security.MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(bytes);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no está disponible", e);
        }
    }

    private static void rollback(java.sql.Connection c, SQLException original) {
        try { c.rollback(); } catch (SQLException rollbackError) { original.addSuppressed(rollbackError); }
    }
}
