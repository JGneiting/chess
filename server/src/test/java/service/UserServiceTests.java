package service;

import model.*;
import org.eclipse.jetty.util.log.Log;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.ServiceError;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTests {

    @BeforeEach
    public void resetDatabase() {
        // Reset the database
        DatabaseService dbService = new DatabaseService();
        dbService.clearDatabase();
    }

    @Test
    public void testRegistration() {
        UserService service = new UserService();

        // Form positive request object
        RegisterRequest posRequest = new RegisterRequest("tester12", "qwerty", "test@test.com");
        RegisterResult result = service.register(posRequest);

        // Assert that we have a response echoing our username and has an auth token
        assertEquals("tester12", result.username());
        assertNotEquals("", result.authToken());

        // try to register the same user again
        ServiceError error = assertThrows(ServiceError.class, () -> service.register(posRequest));
        assertEquals(403, error.getCode());
        assertEquals("Error: already taken", error.getMessage());

        // Try to register a malformed request object
        RegisterRequest negRequest = new RegisterRequest("", "11111", "imanemail@hotmail.com");
        ServiceError badRequest = assertThrows(ServiceError.class, () -> service.register(negRequest));
        assertEquals(400, badRequest.getCode());
        assertEquals("Error: bad request", badRequest.getMessage());
    }

    @Test
    public void testLogin() {
        UserService service = new UserService();

        // Create a user in the database
        RegisterRequest posRequest = new RegisterRequest("tester12", "qwerty", "test@test.com");
        service.register(posRequest);

        // Log the new user in
        LoginRequest request = new LoginRequest("tester12", "qwerty");
        LoginResult result = service.login(request);

        // Assert that we have a response echoing our username and has an auth token
        assertEquals("tester12", result.username());
        assertNotEquals("", result.authToken());

        // Attempt to log the user in with the wrong password
        LoginRequest badRequest = new LoginRequest("tester12", "qwertyuiop");
        ServiceError error = assertThrows(ServiceError.class, () -> service.login(badRequest));
        assertEquals(401, error.getCode());
        assertEquals("Error: unauthorized", error.getMessage());
    }

    @Test
    public void testLogout() {
        UserService service = new UserService();

        // Create a user in the database
        RegisterRequest posRequest = new RegisterRequest("tester12", "qwerty", "test@test.com");
        RegisterResult result = service.register(posRequest);

        // Log the user out
        LogoutRequest request = new LogoutRequest(result.authToken());
        assertDoesNotThrow(() -> service.logout(request));

        // Attempt to log out the user again
        ServiceError error = assertThrows(ServiceError.class, () -> service.logout(request));
        assertEquals(401, error.getCode());
        assertEquals("Error: unauthorized", error.getMessage());
    }
}
