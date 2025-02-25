package service;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTests {

    @BeforeEach
    public void resetDatabase() {
        // Reset the database
        DatabaseService.clearDatabase();
    }

    @Test
    public void testRegistration() {
        // Form positive request object
        RegisterRequest posRequest = new RegisterRequest("tester12", "qwerty", "test@test.com");
        RegisterResult result = UserService.register(posRequest);

        // Assert that we have a response echoing our username and has an auth token
        assertEquals("tester12", result.username());
        assertNotEquals("", result.authToken());

        // try to register the same user again
        ServiceError error = assertThrows(ServiceError.class, () -> UserService.register(posRequest));
        assertEquals(403, error.getCode());
        assertEquals("Error: already taken", error.getMessage());

        // Try to register a malformed request object
        RegisterRequest negRequest = new RegisterRequest("", "11111", "imanemail@hotmail.com");
        ServiceError badRequest = assertThrows(ServiceError.class, () -> UserService.register(negRequest));
        assertEquals(400, badRequest.getCode());
        assertEquals("Error: bad request", badRequest.getMessage());
    }

    @Test
    public void testLogin() {
        // Create a user in the database
        RegisterRequest posRequest = new RegisterRequest("tester12", "qwerty", "test@test.com");
        UserService.register(posRequest);

        // Log the new user in
        LoginRequest request = new LoginRequest("tester12", "qwerty");
        LoginResult result = UserService.login(request);

        // Assert that we have a response echoing our username and has an auth token
        assertEquals("tester12", result.username());
        assertNotEquals("", result.authToken());

        // Attempt to log the user in with the wrong password
        LoginRequest badRequest = new LoginRequest("tester12", "qwertyuiop");
        ServiceError error = assertThrows(ServiceError.class, () -> UserService.login(badRequest));
        assertEquals(401, error.getCode());
        assertEquals("Error: unauthorized", error.getMessage());
    }

    @Test
    public void testLogout() {
        // Create a user in the database
        RegisterRequest posRequest = new RegisterRequest("tester12", "qwerty", "test@test.com");
        RegisterResult result = UserService.register(posRequest);

        // Log the user out
        LogoutRequest request = new LogoutRequest(result.authToken());
        assertDoesNotThrow(() -> UserService.logout(request));

        // Attempt to log out the user again
        ServiceError error = assertThrows(ServiceError.class, () -> UserService.logout(request));
        assertEquals(401, error.getCode());
        assertEquals("Error: unauthorized", error.getMessage());
    }
}
