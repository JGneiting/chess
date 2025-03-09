package service;

import dataaccess.DataAccessException;
import model.*;
import org.junit.jupiter.api.*;

import static org.eclipse.jetty.util.LazyList.size;
import static org.junit.jupiter.api.Assertions.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GameServiceTests {
    static private String userAuthToken;
    static private int gameID;
    static private UserService userService;
    static private GameService gameService;

    static {
        try {
            gameService = new GameService();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        try {
            userService = new UserService();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeAll
    static public void createUser() throws DataAccessException {
        // Clear database
        DatabaseService.clearDatabase();

        // Create a test user
        RegisterRequest request = new RegisterRequest("tester12", "qwerty", "me@me.com");
        RegisterResult result = userService.register(request);
        userAuthToken = result.authToken();
    }

    @Test
    @Order(1)
    public void createGame() {
        // create a game
        NewGameRequest request = new NewGameRequest(userAuthToken, "myGame");
        assertDoesNotThrow(() -> {
            NewGameResult result = gameService.newGame(request);
            assertNotEquals(0, result.gameID());
            gameID = result.gameID();
        });
    }

    @Test
    @Order(2)
    public void createNoName() {
        // Create a game with no name
        NewGameRequest namelessRequest = new NewGameRequest(userAuthToken, "");
        ServiceError error = assertThrows(ServiceError.class, () -> gameService.newGame(namelessRequest));
        assertEquals(400, error.getCode());
        assertEquals("Error: bad request", error.getMessage());
    }

    @Test
    @Order(3)
    public void createNoAuth() {
        // Create a game with no auth token
        NewGameRequest unauthorizedRequest = new NewGameRequest("", "myBetterGame");
        ServiceError noauthError = assertThrows(ServiceError.class, () -> gameService.newGame(unauthorizedRequest));
        assertEquals(401, noauthError.getCode());
        assertEquals("Error: unauthorized", noauthError.getMessage());
    }

    @Test
    @Order(4)
    public void listGames() throws DataAccessException {
        // A game should already exist in the database from the previous test
        ListGamesRequest request = new ListGamesRequest(userAuthToken);
        ListGamesResult result = gameService.listGames(request);

        assertEquals(1, size(result.games()));
        GameData game = result.games()[0];
        assertEquals("myGame", game.gameName());
        assertEquals(gameID, game.gameID());
    }

    @Test
    @Order(5)
    public void listNoAuth() {
        // Request games without an auth token
        ListGamesRequest noauthRequest = new ListGamesRequest("");
        ServiceError error = assertThrows(ServiceError.class, () -> gameService.listGames(noauthRequest));
        assertEquals(401, error.getCode());
        assertEquals("Error: unauthorized", error.getMessage());
    }

    @Test
    @Order(6)
    public void joinGame() throws DataAccessException {
        // A game should exist already, join it
        JoinGameRequest joinWhite = new JoinGameRequest(userAuthToken, "WHITE", gameID);
        assertDoesNotThrow(() -> gameService.joinGame(joinWhite));
        // Verify that data was changed
        ListGamesRequest listRequest = new ListGamesRequest(userAuthToken);
        ListGamesResult listResult = gameService.listGames(listRequest);
        GameData game = listResult.games()[0];

        assertEquals(gameID, game.gameID());
        assertEquals("tester12", game.whiteUsername());
        assertNull(game.blackUsername());
    }

    @Test
    @Order(7)
    public void joinTeamTaken() {
        JoinGameRequest joinWhite = new JoinGameRequest(userAuthToken, "WHITE", gameID);

        // Attempt to join white team again
        ServiceError takenError = assertThrows(ServiceError.class, () -> gameService.joinGame(joinWhite));
        assertEquals(403, takenError.getCode());
        assertEquals("Error: already taken", takenError.getMessage());
    }

    @Test
    @Order(8)
    public void joinNonexistentGame() {
        // Attempt to join nonexistent game
        JoinGameRequest joinBadGame = new JoinGameRequest(userAuthToken, "WHITE", gameID+1);
        ServiceError badRequest = assertThrows(ServiceError.class, () -> gameService.joinGame(joinBadGame));
        assertEquals(400, badRequest.getCode());
        assertEquals("Error: bad request", badRequest.getMessage());
    }

    @Test
    @Order(9)
    public void joinNoAuth() {
        // Join without auth
        JoinGameRequest joinNoauth = new JoinGameRequest("", "BLACK", gameID);
        ServiceError noAuth = assertThrows(ServiceError.class, () -> gameService.joinGame(joinNoauth));
        assertEquals(401, noAuth.getCode());
        assertEquals("Error: unauthorized", noAuth.getMessage());
    }
}
