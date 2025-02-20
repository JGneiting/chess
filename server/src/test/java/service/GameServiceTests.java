package service;

import model.*;
import org.eclipse.jetty.server.Authentication;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static org.eclipse.jetty.util.LazyList.size;
import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTests {
    static private String userAuthToken;
    static private String gameID;

    @BeforeAll
    static public void createUser() {
        UserService service = new UserService();
        RegisterRequest request = new RegisterRequest("tester12", "qwerty", "me@me.com");
        RegisterResult result = service.register(request);
        userAuthToken = result.authToken();
    }

    @Test
    @Order(1)
    public void createGame() {
        GameService service = new GameService();

        // create a game
        NewGameRequest request = new NewGameRequest(userAuthToken, "myGame");
        assertDoesNotThrow(() -> {
            NewGameResult result = service.newGame(request);
            assertNotEquals("", result.gameID());
            gameID = result.gameID();
        });

        // Create a game with no name
        NewGameRequest namelessRequest = new NewGameRequest(userAuthToken, "");
        ServiceError error = assertThrows(ServiceError.class, () -> service.newGame(namelessRequest));
        assertEquals(400, error.getCode());
        assertEquals("Error: bad request", error.getMessage());

        // Create a game with no auth token
        NewGameRequest unauthorizedRequest = new NewGameRequest("", "myBetterGame");
        ServiceError noauthError = assertThrows(ServiceError.class, () -> service.newGame(unauthorizedRequest));
        assertEquals(401, noauthError.getCode());
        assertEquals("Error: unauthorized", error.getMessage());
    }

    @Test
    @Order(2)
    public void listGames() {
        GameService service = new GameService();

        // A game should already exist in the database from the previous test
        ListGamesRequest request = new ListGamesRequest(userAuthToken);
        ListGamesResult result = service.listGames(request);

        assertEquals(1, size(result.games()));
        GameData game = result.games()[0];
        assertEquals("myGame", game.gameName());
        assertEquals(gameID, game.gameID());

        // Request games without an auth token
        ListGamesRequest noauthRequest = new ListGamesRequest("");
        ServiceError error = assertThrows(ServiceError.class, () -> service.listGames(noauthRequest));
        assertEquals(401, error.getCode());
        assertEquals("Error: unauthorized", error.getMessage());
    }

    @Test
    @Order(3)
    public void joinGame() {
        GameService service = new GameService();

        // A game should exist already, join it
        JoinGameRequest joinWhite = new JoinGameRequest(userAuthToken, "WHITE", gameID);
        assertDoesNotThrow(() -> {
            service.joinGame(joinWhite);
        });
        // Verify that data was changed
        ListGamesRequest listRequest = new ListGamesRequest(userAuthToken);
        ListGamesResult listResult = service.listGames(listRequest);
        GameData game = listResult.games()[0];

        assertEquals(gameID, game.gameID());
        assertEquals("tester12", game.whiteUsername());
        assertEquals("", game.blackUsername());

        // Attempt to join white team again
        ServiceError takenError = assertThrows(ServiceError.class, () -> service.joinGame(joinWhite));
        assertEquals(403, takenError.getCode());
        assertEquals("Error: already taken", takenError.getMessage());

        // Attempt to join nonexistent game
        JoinGameRequest joinBadGame = new JoinGameRequest(userAuthToken, "WHITE", "aaaa");
        ServiceError badRequest = assertThrows(ServiceError.class, () -> service.joinGame(joinBadGame));
        assertEquals(400, badRequest.getCode());
        assertEquals("Error: bad request", badRequest.getMessage());

        // Join without auth
        JoinGameRequest joinNoauth = new JoinGameRequest("", "BLACK", gameID);
        ServiceError noAuth = assertThrows(ServiceError.class, () -> service.joinGame(joinNoauth));
        assertEquals(401, noAuth.getCode());
        assertEquals("Error: unauthorized", noAuth.getMessage());
    }
}
