package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {
    private static final String URL = "jdbc:mysql://localhost:3306/baith2?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            return conn;
        } catch (ClassNotFoundException e) {
            System.err.println("Không tìm thấy JDBC Driver: " + e.getMessage());
            throw new SQLException("Driver not found", e);
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối đến database: " + e.getMessage());
            throw e;
        }
    }
}
