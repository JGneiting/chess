package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthDBTests {

    @BeforeAll
    static public void setup() {
        // Clear the auth database
        assertDoesNotThrow(() -> {
            AuthDAO authDAO = new SQLAuthDAO();
            authDAO.clear();
        });
    }

    @AfterAll
    static public void clear() {
        // Clear the auth database
        assertDoesNotThrow(() -> {
            AuthDAO authDAO = new SQLAuthDAO();
            authDAO.clear();
        });
    }

    @Test
    @Order(1)
    public void createUser() {
        assertDoesNotThrow(() -> {
            AuthDAO authDAO = new SQLAuthDAO();
            AuthData authData = new AuthData("qwerty", "Tester12");
            authDAO.createAuth(authData);
        });
    }

    @Test
    @Order(2)
    public void createDuplicateUser() {
        assertDoesNotThrow(() -> {
            AuthDAO authDAO = new SQLAuthDAO();
            AuthData authData = new AuthData("qwerty12", "Tester12");
            authDAO.createAuth(authData);

            // Make sure the auth is updated
            AuthData updatedAuth = authDAO.getAuth("qwerty12");
            assertNotNull(updatedAuth);
        });
    }

    @Test
    @Order(3)
    public void getAuth() {
        assertDoesNotThrow(() -> {
            AuthDAO authDAO = new SQLAuthDAO();
            AuthData authData = authDAO.getAuth("qwerty12");
            assertNotNull(authData);
            assertEquals("Tester12", authData.username());
        });
    }

    @Test
    @Order(4)
    public void requestBadAuth() {
        assertDoesNotThrow(() -> {
            AuthDAO authDAO = new SQLAuthDAO();
            AuthData authData = authDAO.getAuth("badAuth");
            assertNull(authData);
        });
    }

    @Test
    @Order(5)
    public void deleteAuth() {
        assertDoesNotThrow(() -> {
            AuthDAO authDAO = new SQLAuthDAO();
            AuthData authData = authDAO.getAuth("qwerty12");
            authDAO.deleteAuth(authData);

            // Make sure the auth is deleted
            AuthData deletedAuth = authDAO.getAuth("qwerty12");
            assertNull(deletedAuth);
        });
    }

    @Test
    @Order(6)
    public void deleteBadAuth() {
        assertDoesNotThrow(() -> {
            AuthDAO authDAO = new SQLAuthDAO();
            AuthData authData = new AuthData("qwertyUIOP", "Tester12");
            assertThrows(DataAccessException.class, () -> {
                authDAO.deleteAuth(authData);
            });
        });
    }

    @Test
    @Order(7)
    public void clearAuth() {
        assertDoesNotThrow(() -> {
            AuthDAO authDAO = new SQLAuthDAO();
            authDAO.clear();

            // Make sure the auth database is empty
            AuthData authData = authDAO.getAuth("qwerty");
            assertNull(authData);
        });
    }
}
