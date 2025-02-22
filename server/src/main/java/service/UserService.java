package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import jdk.jshell.spi.ExecutionControl;
import model.*;

import java.util.UUID;

public class UserService {
    static MemoryUserDAO userDB = new MemoryUserDAO();
    static MemoryAuthDAO authDB = new MemoryAuthDAO();

    private String generateAuthToken() {
        return UUID.randomUUID().toString();
    }

    static public AuthData checkAuth(String authToken) throws ServiceError {
        AuthData locatedAuth = authDB.getAuth(authToken);
        if (locatedAuth == null) {
            throw new ServiceError("Error: unauthorized", 401);
        }
        return locatedAuth;
    }

    public RegisterResult register(RegisterRequest registerRequest) throws ServiceError {
        // Validate that all required information is in the register request
        if (registerRequest.username().isEmpty() || registerRequest.password().isEmpty() || registerRequest.email().isEmpty()) {
            // Bad Request
            throw new ServiceError("Error: bad request", 400);
        }

        // Try to get the user with the request username to see if it exists
        UserData locatedUser = userDB.getUser(registerRequest.username());

        if (locatedUser != null) {
            // Username already taken
            throw new ServiceError("Error: already taken", 403);
        }

        // Create a new user and auth data
        UserData user = new UserData(registerRequest.username(), registerRequest.password(), registerRequest.email());
        AuthData auth = new AuthData(generateAuthToken(), user.username());
        userDB.createUser(user);
        authDB.createAuth(auth);

        // Create response object
        return new RegisterResult(user.username(), auth.authToken());
    }

    public LoginResult login(LoginRequest loginRequest) throws ServiceError {
        // Find user
        UserData user = userDB.getUser(loginRequest.username());

        // Check if the user submitted the correct password
        if (!user.password().equals(loginRequest.password())) {
            throw new ServiceError("Error: unauthorized", 401);
        }

        // Create auth data
        AuthData auth = new AuthData(generateAuthToken(), user.username());
        authDB.createAuth(auth);

        // Return response
        return new LoginResult(user.username(), auth.authToken());
    }

    public void logout(LogoutRequest logoutRequest) throws ServiceError{
        // Check if the user has auth data in the database
        AuthData auth = checkAuth(logoutRequest.authToken());

        try {
            authDB.deleteAuth(auth);
        } catch (DataAccessException e) {
            throw new ServiceError("Error: could not delete auth", 500);
        }
    }
}
