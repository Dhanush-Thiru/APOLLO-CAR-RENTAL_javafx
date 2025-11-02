import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class for establishing and managing the MySQL database connection.
 * Used by CarRentalAdminApp for all database operations.
 */
public class DBUtil {

    // !! IMPORTANT: Change these values to match your MySQL setup !!
    // The database name MUST match the 'car_rental_db' you created.
    private static final String DB_URL = "jdbc:mysql://localhost:3306/car_rental_db?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";       // MySQL username
    private static final String DB_PASS = "dhanush2006"; // <-- REPLACE THIS WITH YOUR ACTUAL PASSWORD!

    /**
     * Establishes a connection to the MySQL database.
     * @return A valid Connection object.
     * @throws SQLException if a database access error occurs.
     */
    public static Connection getConnection() throws SQLException {
        // The driver (com.mysql.cj.jdbc.Driver) is automatically registered by the JDBC manager 
        // since we included mysql-connector-java in pom.xml.
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }
    
    /**
     * Utility method to safely close a database connection.
     * @param conn The Connection object to close.
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}
