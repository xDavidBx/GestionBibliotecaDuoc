package JdbcDAO;

import config.DataBaseConnection;
import DAO.UsuarioDAO;
import modelo.Usuario;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.HexFormat;
import java.util.Optional;

public final class JdbcUsuarioDAO implements UsuarioDAO {
    private final DataBaseConnection database;

    public JdbcUsuarioDAO(DataBaseConnection database) { this.database = database; }

    @Override public Optional<Usuario> autenticar(String correo, String contrasena) throws SQLException {
        String sql = "SELECT u.id, u.nombre, u.rut, u.correo, u.rol, e.id AS id_estudiante " +
                "FROM usuarios u LEFT JOIN estudiantes e ON e.rut = u.rut " +
                "WHERE LOWER(u.correo)=LOWER(?) AND u.contrasena=?";
        try (var connection = database.getConnection(); var statement = connection.prepareStatement(sql)) {
            statement.setString(1, correo.trim());
            statement.setString(2, sha256(contrasena));
            try (var rs = statement.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                int studentId = rs.getInt("id_estudiante");
                Integer idEstudiante = rs.wasNull() ? null : studentId;
                return Optional.of(new Usuario(rs.getInt("id"), rs.getString("nombre"), rs.getString("rut"),
                        rs.getString("correo"), rs.getString("rol"), idEstudiante));
            }
        }
    }

    @Override public List<Usuario> listar() throws SQLException {
        String sql = "SELECT u.id,u.nombre,u.rut,u.correo,u.rol,e.id AS id_estudiante FROM usuarios u " +
                "LEFT JOIN estudiantes e ON e.rut=u.rut ORDER BY u.nombre";
        List<Usuario> result = new ArrayList<>();
        try (var c = database.getConnection(); var s = c.prepareStatement(sql); var rs = s.executeQuery()) {
            while (rs.next()) {
                int studentId = rs.getInt("id_estudiante");
                boolean noStudent = rs.wasNull();
                result.add(new Usuario(rs.getInt("id"), rs.getString("nombre"), rs.getString("rut"),
                        rs.getString("correo"), rs.getString("rol"), noStudent ? null : studentId));
            }
        }
        return result;
    }

    @Override public void crear(Usuario u, String contrasena) throws SQLException {
        String sql = "INSERT INTO usuarios(nombre,rut,correo,contrasena,rol) VALUES (?,?,?,?,?)";
        try (var c = database.getConnection(); var s = c.prepareStatement(sql)) {
            s.setString(1, u.nombre().trim()); s.setString(2, u.rut().trim()); s.setString(3, u.correo().trim());
            s.setString(4, sha256(contrasena)); s.setString(5, u.rol().trim().toUpperCase()); s.executeUpdate();
        }
    }

    @Override public void actualizar(Usuario u, String contrasenaNueva) throws SQLException {
        String sql = contrasenaNueva == null || contrasenaNueva.isBlank()
                ? "UPDATE usuarios SET nombre=?,rut=?,correo=?,rol=? WHERE id=?"
                : "UPDATE usuarios SET nombre=?,rut=?,correo=?,rol=?,contrasena=? WHERE id=?";
        try (var c = database.getConnection(); var s = c.prepareStatement(sql)) {
            s.setString(1, u.nombre().trim()); s.setString(2, u.rut().trim()); s.setString(3, u.correo().trim()); s.setString(4, u.rol().trim().toUpperCase());
            if (contrasenaNueva == null || contrasenaNueva.isBlank()) s.setInt(5, u.id());
            else { s.setString(5, sha256(contrasenaNueva)); s.setInt(6, u.id()); }
            s.executeUpdate();
        }
    }

    @Override public void eliminar(int id) throws SQLException {
        try (var c = database.getConnection(); var s = c.prepareStatement("DELETE FROM usuarios WHERE id=?")) {
            s.setInt(1, id); s.executeUpdate();
        }
    }

    private static String sha256(String value) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no está disponible en esta JVM", e);
        }
    }
}
