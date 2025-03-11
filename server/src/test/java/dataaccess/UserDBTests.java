package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;
import service.DatabaseService;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDBTests {

    @BeforeAll
    public static void setup() throws DataAccessException {
        // Setup the database
        DatabaseService.clearDatabase();
    }

    @Test
    @Order(1)
    public void createUser() {
        assertDoesNotThrow(() -> {
            SQLUserDAO userDB = new SQLUserDAO();
            UserData user = new UserData("testUser", "password", "test@test.com");
            userDB.createUser(user);
        });
    }

    @Test
    @Order(2)
    public void createDuplicateUser() {
        assertThrows(DataAccessException.class, () -> {
            SQLUserDAO userDB = new SQLUserDAO();
            UserData user = new UserData("testUser", "password", "test12@test.com");
            userDB.createUser(user);
        });
    }

    @Test
    @Order(3)
    public void getUser() {
        assertDoesNotThrow(() -> {
            SQLUserDAO userDB = new SQLUserDAO();
            UserData user = userDB.getUser("testUser");
            assertNotNull(user);
            assertEquals("password", user.password());
            assertEquals("test@test.com", user.email());
        });
    }

    @Test
    @Order(4)
    public void getNonExistentUser() {
        assertDoesNotThrow(() -> {
            SQLUserDAO userDB = new SQLUserDAO();
            UserData user = userDB.getUser("nonExistentUser");
            assertNull(user);
        });
    }

    @Test
    @Order(5)
    public void clear() {
        assertDoesNotThrow(() -> {
            SQLUserDAO userDB = new SQLUserDAO();
            userDB.clear();
        });
    }
}
