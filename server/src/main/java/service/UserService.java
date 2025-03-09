package service;

import dataaccess.*;
import model.*;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class UserService {
    private final SQLUserDAO userDB;
    private final SQLAuthDAO authDB;

    public UserService() throws DataAccessException {
        userDB = new SQLUserDAO();
        authDB = new SQLAuthDAO();
    }

    private static String generateAuthToken() {
        return UUID.randomUUID().toString();
    }

    public AuthData checkAuth(String authToken) throws ServiceError, DataAccessException {
        AuthData locatedAuth = authDB.getAuth(authToken);
        if (locatedAuth == null) {
            throw new ServiceError("Error: unauthorized", 401);
        }
        return locatedAuth;
    }

    public RegisterResult register(RegisterRequest registerRequest) throws ServiceError, DataAccessException {
        // Validate that all required information is in the register request
        if (registerRequest.username() == null || registerRequest.password() == null || registerRequest.email() == null ||
                registerRequest.username().isEmpty() || registerRequest.password().isEmpty() || registerRequest.email().isEmpty()) {
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
        String hashedPassword = BCrypt.hashpw(registerRequest.password(), BCrypt.gensalt());
        UserData user = new UserData(registerRequest.username(), hashedPassword, registerRequest.email());
        AuthData auth = new AuthData(generateAuthToken(), user.username());
        userDB.createUser(user);
        authDB.createAuth(auth);

        // Create response object
        return new RegisterResult(user.username(), auth.authToken());
    }

    public LoginResult login(LoginRequest loginRequest) throws ServiceError, DataAccessException {
        // Find user
        UserData user = userDB.getUser(loginRequest.username());

        // Check if the user submitted the correct password
        if (user == null || !BCrypt.checkpw(loginRequest.password(), user.password())) {
            throw new ServiceError("Error: unauthorized", 401);
        }

        // Create auth data
        AuthData auth = new AuthData(generateAuthToken(), user.username());
        authDB.createAuth(auth);

        // Return response
        return new LoginResult(user.username(), auth.authToken());
    }

    public void logout(LogoutRequest logoutRequest) throws ServiceError, DataAccessException{
        // Check if the user has auth data in the database
        AuthData auth = checkAuth(logoutRequest.authToken());

        authDB.deleteAuth(auth);
    }
}
