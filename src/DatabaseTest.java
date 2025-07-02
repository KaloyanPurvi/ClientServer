import org.junit.jupiter.api.*;

import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseTest {

    private static final String testName = "   ";
    private static final String testEmail = " @example.com";
    private static final String testPassword = " ";

    @BeforeEach
    public void resetUser() {
        try (var conn = DriverManager.getConnection(
                " ", " ", " ");
             var stmt = conn.prepareStatement("DELETE FROM users WHERE email = ?")) {
            stmt.setString(1, testEmail);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Неуспех - потребителя от тестовата база не беше изтрит.");
        }
    }

    @Test
    public void testRegisterNewUser() {
        String result = Database.registerUser(testName, testEmail, testPassword);
        assertEquals("Успешна регистрация!", result);
    }

    @Test
    public void testDuplicateEmailRegistration() {
        // Регистрация веднъж
        Database.registerUser(testName, testEmail, testPassword);
        // Втора регистрация със същия имейл
        String second = Database.registerUser(testName, testEmail, testPassword);
        assertEquals("Имейлът вече съществува.", second);
    }

    @Test
    public void testCorrectLogin() {
        Database.registerUser(testName, testEmail, testPassword);
        boolean login = Database.checkCredentials(testEmail, testPassword);
        assertTrue(login);
    }

    @Test
    public void testIncorrectLogin() {
        Database.registerUser(testName, testEmail, testPassword);
        boolean login = Database.checkCredentials(testEmail, "wrongpassword");
        assertFalse(login);
    }
}
