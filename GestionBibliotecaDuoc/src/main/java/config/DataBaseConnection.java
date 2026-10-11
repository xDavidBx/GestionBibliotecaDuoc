package config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


public final class DataBaseConnection {
    private static final DataBaseConnection INSTANCE = new DataBaseConnection();
    private final Properties properties = new Properties();

    private DataBaseConnection() {
        Path configPath = Path.of("config.properties");
        if (Files.exists(configPath)) {
            try (InputStream input = Files.newInputStream(configPath)) {
                properties.load(input);
            } catch (IOException e) {
                throw new IllegalStateException("No se pudo leer config.properties", e);
            }
        }
    }

    public static DataBaseConnection getInstance() {
        return INSTANCE;
    }

    public Connection getConnection() throws SQLException {
        String url = setting("DB_URL", "db.url",
                "jdbc:mysql://localhost:3306/biblioteca_escolar?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Santiago&characterEncoding=UTF-8");
        String user = setting("DB_USER", "db.user", "root");
        String password = setting("DB_PASSWORD", "db.password",  "ADMIN");
        return DriverManager.getConnection(url, user, password);
    }

    private String setting(String env, String property, String fallback) {
        String fromEnvironment = System.getenv(env);
        if (fromEnvironment != null && !fromEnvironment.isBlank()) return fromEnvironment;
        return properties.getProperty(property, fallback);
    }
}