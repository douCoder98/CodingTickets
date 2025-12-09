package fr.coding.tickets.dao.jdbc;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static String url;
    private static String username;
    private static String password;

    static {
        loadProperties();
        loadDriver();
    }

    /**
     * Charge la configuration depuis les variables d'environnement (Docker)
     */
    private static void loadProperties() {
        String dbHost = System.getenv("DB_HOST");
        String dbPort = System.getenv("DB_PORT");
        String dbName = System.getenv("DB_NAME");
        String dbUser = System.getenv("DB_USER");
        String dbPassword = System.getenv("DB_PASSWORD");

        // Si variables d'environnement présentes (Docker)
        if (dbHost != null && dbName != null && dbUser != null) {
            url = String.format("jdbc:postgresql://%s:%s/%s",
                               dbHost,
                               dbPort != null ? dbPort : "5432",
                               dbName);
            username = dbUser;
            password = dbPassword;
            System.out.println("✓ JDBC configuré depuis Docker");
            System.out.println("  URL: " + url);
            System.out.println("  User: " + username);
        } else {
            // Sinon valeurs par défaut (développement local)
            url = "jdbc:postgresql://localhost:5432/coding_tickets";
            username = "postgres";
            password = "postgres";
            System.out.println("⚠️ JDBC configuré en mode local");
        }
    }

    /**
     * Charge le driver JDBC PostgreSQL
     */
    private static void loadDriver() {
        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("✓ Driver PostgreSQL chargé");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver JDBC PostgreSQL introuvable", e);
        }
    }

    /**
     * Obtient une nouvelle connexion à la base de données
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Ferme proprement une connexion
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Erreur fermeture : " + e.getMessage());
            }
        }
    }
}