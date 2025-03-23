package client;

import dataaccess.DataAccessException;
import model.*;
import org.junit.jupiter.api.*;
import server.Server;
import service.ServiceError;
import ui.ResponseException;
import ui.ServerFacade;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void setUp() {
        // clear the database
        facade.clearApplication();
    }

    private String createAuthUser() {
        RegisterRequest request = new RegisterRequest("tester12", "qwerty", "test@test.com");
        RegisterResult result = facade.register(request);
        return result.authToken();
    }

    private int createTestGame(String authToken) {
        NewGameRequest request = new NewGameRequest(authToken, "myGame");
        NewGameResult result = facade.createGame(request);
        return result.gameID();
    }

    @Test
    public void testRegistration(){
        // Form positive request object
        RegisterRequest posRequest = new RegisterRequest("tester12", "qwerty", "test@test.com");
        RegisterResult result = facade.register(posRequest);

        // Assert that we have a response echoing our username and has an auth token
        assertEquals("tester12", result.username());
        assertNotEquals("", result.authToken());
    }

    @Test
    public void registerDuplicateUser() {
        RegisterRequest request = new RegisterRequest("tester12", "qwerty", "test@test.com");

        // try to register the same user again
        ResponseException error = assertThrows(ResponseException.class, () -> facade.register(request));
        assertEquals(403, error.getCode());
        assertEquals("Error: already taken", error.getMessage());
    }

    @Test
    public void registerBadRequest() {
        // Try to register a malformed request object
        RegisterRequest negRequest = new RegisterRequest("", "11111", "imanemail@hotmail.com");
        ResponseException badRequest = assertThrows(ResponseException.class, () -> facade.register(negRequest));
        assertEquals(400, badRequest.getCode());
        assertEquals("Error: bad request", badRequest.getMessage());
    }

    @Test
    public void testLogin() {
        // register the tester12 user
        testRegistration();
        // Log the new user in
        LoginRequest request = new LoginRequest("tester12", "qwerty");
        LoginResult result = facade.login(request);

        // Assert that we have a response echoing our username and has an auth token
        assertEquals("tester12", result.username());
        assertNotEquals("", result.authToken());
    }

    @Test
    public void loginBadPassword() {
        // register the tester12 user
        testRegistration();
        // Attempt to log the user in with the wrong password
        LoginRequest badRequest = new LoginRequest("tester12", "qwertyuiop");
        ResponseException error = assertThrows(ResponseException.class, () -> facade.login(badRequest));
        assertEquals(401, error.getCode());
        assertEquals("Error: unauthorized", error.getMessage());
    }

    @Test
    public void testLogout() {
        // register the tester12 user
        String authToken = createAuthUser();
        // Log the user out
        LogoutRequest request = new LogoutRequest(authToken);
        assertDoesNotThrow(() -> facade.logout(request));
    }

    @Test
    public void logoutAgain() {
        // register the tester12 user
        String authToken = createAuthUser();
        LogoutRequest request = new LogoutRequest(authToken);

        assertDoesNotThrow(() -> facade.logout(request));
        // Attempt to log out the user again
        ResponseException error = assertThrows(ResponseException.class, () -> facade.logout(request));
        assertEquals(401, error.getCode());
        assertEquals("Error: unauthorized", error.getMessage());
    }

    @Test
    public void createGame() {
        // register the tester12 user
        String authToken = createAuthUser();

        // create a game
        NewGameRequest request = new NewGameRequest(authToken, "myGame");
        assertDoesNotThrow(() -> {
            NewGameResult result = facade.createGame(request);
            assertNotEquals(0, result.gameID());
        });
    }

    @Test
    public void createNoName() {
        // register the tester12 user
        String authToken = createAuthUser();

        // Create a game with no name
        NewGameRequest namelessRequest = new NewGameRequest(authToken, "");
        ResponseException error = assertThrows(ResponseException.class, () -> facade.createGame(namelessRequest));
        assertEquals(400, error.getCode());
        assertEquals("Error: bad request", error.getMessage());
    }

    @Test
    public void createNoAuth() {
        // Create a game with no auth token
        NewGameRequest unauthorizedRequest = new NewGameRequest("", "myBetterGame");
        ResponseException noauthError = assertThrows(ResponseException.class, () -> facade.createGame(unauthorizedRequest));
        assertEquals(401, noauthError.getCode());
        assertEquals("Error: unauthorized", noauthError.getMessage());
    }

    @Test
    public void listGames() throws DataAccessException {
        // register the tester12 user
        String authToken = createAuthUser();
        int gameID = createTestGame(authToken);

        // A game should already exist in the database from the previous test
        ListGamesRequest request = new ListGamesRequest(authToken);
        ListGamesResult result = facade.listGames(request);

        assertEquals(1, result.games().length);
        GameData game = result.games()[0];
        assertEquals("myGame", game.gameName());
        assertEquals(gameID, game.gameID());
    }

    @Test
    public void listNoAuth() {
        // Request games without an auth token
        ListGamesRequest noauthRequest = new ListGamesRequest("");
        ResponseException error = assertThrows(ResponseException.class, () -> facade.listGames(noauthRequest));
        assertEquals(401, error.getCode());
        assertEquals("Error: unauthorized", error.getMessage());
    }

    @Test
    public void joinGame() throws DataAccessException {
        // register the tester12 user
        String authToken = createAuthUser();
        int gameID = createTestGame(authToken);

        // A game should exist already, join it
        JoinGameRequest joinWhite = new JoinGameRequest(authToken, "WHITE", gameID);
        assertDoesNotThrow(() -> facade.joinGame(joinWhite));
        // Verify that data was changed
        ListGamesRequest listRequest = new ListGamesRequest(authToken);
        ListGamesResult listResult = facade.listGames(listRequest);
        GameData game = listResult.games()[0];

        assertEquals(gameID, game.gameID());
        assertEquals("tester12", game.whiteUsername());
        assertNull(game.blackUsername());
    }

    @Test
    public void joinTeamTaken() {
        // register the tester12 user
        String authToken = createAuthUser();
        int gameID = createTestGame(authToken);

        JoinGameRequest joinWhite = new JoinGameRequest(authToken, "WHITE", gameID);
        assertDoesNotThrow(() -> facade.joinGame(joinWhite));

        // Attempt to join white team again
        ServiceError takenError = assertThrows(ServiceError.class, () -> facade.joinGame(joinWhite));
        assertEquals(403, takenError.getCode());
        assertEquals("Error: already taken", takenError.getMessage());
    }

    @Test
    public void joinNonexistentGame() {
        // register the tester12 user
        String authToken = createAuthUser();
        int gameID = createTestGame(authToken);

        // Attempt to join nonexistent game
        JoinGameRequest joinBadGame = new JoinGameRequest(authToken, "WHITE", gameID+1);
        ResponseException badRequest = assertThrows(ResponseException.class, () -> facade.joinGame(joinBadGame));
        assertEquals(400, badRequest.getCode());
        assertEquals("Error: bad request", badRequest.getMessage());
    }

    @Test
    public void joinNoAuth() {
        // register the tester12 user
        String authToken = createAuthUser();
        int gameID = createTestGame(authToken);

        // Join without auth
        JoinGameRequest joinNoauth = new JoinGameRequest("", "BLACK", gameID);
        ResponseException noAuth = assertThrows(ResponseException.class, () -> facade.joinGame(joinNoauth));
        assertEquals(401, noAuth.getCode());
        assertEquals("Error: unauthorized", noAuth.getMessage());
    }

}
