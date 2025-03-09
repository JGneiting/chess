package service;

import dataaccess.DataAccessException;
import model.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserServiceTests {
    private static String authToken;
    private static UserService userService;

    static {
        try {
            userService = new UserService();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeAll
    public static void resetDB() throws DataAccessException {
        DatabaseService.clearDatabase();
    }

    @Test
    @Order(1)
    public void testRegistration() throws DataAccessException {
        // Form positive request object
        RegisterRequest posRequest = new RegisterRequest("tester12", "qwerty", "test@test.com");
        RegisterResult result = userService.register(posRequest);

        // Assert that we have a response echoing our username and has an auth token
        assertEquals("tester12", result.username());
        assertNotEquals("", result.authToken());
    }

    @Test
    @Order(2)
    public void registerDuplicateUser() {
        RegisterRequest request = new RegisterRequest("tester12", "qwerty", "test@test.com");

        // try to register the same user again
        ServiceError error = assertThrows(ServiceError.class, () -> userService.register(request));
        assertEquals(403, error.getCode());
        assertEquals("Error: already taken", error.getMessage());
    }

    @Test
    @Order(3)
    public void registerBadRequest() {
        // Try to register a malformed request object
        RegisterRequest negRequest = new RegisterRequest("", "11111", "imanemail@hotmail.com");
        ServiceError badRequest = assertThrows(ServiceError.class, () -> userService.register(negRequest));
        assertEquals(400, badRequest.getCode());
        assertEquals("Error: bad request", badRequest.getMessage());
    }

    @Test
    @Order(4)
    public void testLogin() throws DataAccessException {
        // Log the new user in
        LoginRequest request = new LoginRequest("tester12", "qwerty");
        LoginResult result = userService.login(request);

        // Assert that we have a response echoing our username and has an auth token
        assertEquals("tester12", result.username());
        assertNotEquals("", result.authToken());

        authToken = result.authToken();
    }

    @Test
    @Order(5)
    public void loginBadPassword() {
        // Attempt to log the user in with the wrong password
        LoginRequest badRequest = new LoginRequest("tester12", "qwertyuiop");
        ServiceError error = assertThrows(ServiceError.class, () -> userService.login(badRequest));
        assertEquals(401, error.getCode());
        assertEquals("Error: unauthorized", error.getMessage());
    }

    @Test
    @Order(6)
    public void testLogout() {
        // Log the user out
        LogoutRequest request = new LogoutRequest(authToken);
        assertDoesNotThrow(() -> userService.logout(request));
    }

    @Test
    @Order(7)
    public void logoutAgain() {
        LogoutRequest request = new LogoutRequest(authToken);

        // Attempt to log out the user again
        ServiceError error = assertThrows(ServiceError.class, () -> userService.logout(request));
        assertEquals(401, error.getCode());
        assertEquals("Error: unauthorized", error.getMessage());
    }
}
