import java.sql.*;

public class Database {
    // Константи за връзка към MySQL
    private static final String DB_URL = "";  // адрес на базата
    private static final String DB_USER = "";                                 // потребителско име
    private static final String DB_PASSWORD = "";                    // парола

    //метод за логин

    public static boolean checkCredentials(String email, String password) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE email = ? AND password = ?")) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            System.out.println("DEBUG login: email=" + email + ", password=" + password);

            return rs.next(); // връща true ако има съвпадение
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



    // Метод за запис на потребител
    public static String registerUser(String name, String email, String password) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, name.trim());
            stmt.setString(2, email.trim());
            stmt.setString(3, password.trim());

            int rows = stmt.executeUpdate();
            return rows > 0 ? "Успешна регистрация!" : "Неуспешно добавяне.";
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                return "Имейлът вече съществува.";
            }
            e.printStackTrace();
            return "Грешка при регистрация.";
        }
    }

}
