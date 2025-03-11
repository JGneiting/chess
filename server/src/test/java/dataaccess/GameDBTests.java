package dataaccess;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import model.GameData;
import org.junit.jupiter.api.*;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GameDBTests {

    @BeforeAll
    public static void setup() throws DataAccessException {
        // Clear the database
        SQLGameDAO gameDB = new SQLGameDAO();
        gameDB.clear();

    }

    @Test
    @Order(1)
    public void createGame(){
        // Create a game
        ChessGame game = new ChessGame();
        GameData gameData = new GameData(game, "Test Game", null, null, 1111);

        assertDoesNotThrow(() -> {

            // Add the game to the database
            SQLGameDAO gameDB = new SQLGameDAO();
            gameDB.createGame(gameData);
        });
    }

    @Test
    @Order(2)
    public void createDuplicateGame() {
        // Create a game
        ChessGame game = new ChessGame();
        GameData gameData = new GameData(game, "Cool new game", null, null, 1111);

        assertThrows(DataAccessException.class, () -> {

            // Add the game to the database
            SQLGameDAO gameDB = new SQLGameDAO();
            gameDB.createGame(gameData);
        });
    }

    @Test
    @Order(3)
    public void getGame() {
        assertDoesNotThrow(() -> {
            // Get the game from the database;
            SQLGameDAO gameDB = new SQLGameDAO();
            GameData gameData = gameDB.getGame(1111);

            assertNotNull(gameData, "Game not found in database");
        });
    }

    @Test
    @Order(4)
    public void getNonExistentGame() {
        assertDoesNotThrow(() -> {
            // Get the game from the database;
            SQLGameDAO gameDB = new SQLGameDAO();
            GameData gameData = gameDB.getGame(9999);
            assertNull(gameData, "Game found in database");
        });
    }

    @Test
    @Order(5)
    public void listGames() {
        assertDoesNotThrow(() -> {
            // Add another game to the database
            ChessGame game2 = new ChessGame();
            GameData gameData2 = new GameData(game2, "Another Game", null, null, 2222);

            SQLGameDAO gameDB = new SQLGameDAO();
            gameDB.createGame(gameData2);

            Collection<GameData> games = gameDB.listGames();
            assertEquals(2, games.size(), "Incorrect number of games in database");
        });
    }

    @Test
    @Order(6)
    public void updateGame() {
        assertDoesNotThrow(() -> {
            ChessGame game = new ChessGame();
            // Get the game from the database;
            SQLGameDAO gameDB = new SQLGameDAO();
            ChessMove move = new ChessMove(new ChessPosition(1, 2), new ChessPosition(3, 3), null);
            game.makeMove(move);

            // Update game in the database
            gameDB.updateGame(new GameData(game, "Test Game", "tester12", null, 1111));
        });
    }

    @Test
    @Order(7)
    public void updateNonExistentGame() {
        assertThrows(DataAccessException.class, () -> {
            ChessGame game = new ChessGame();
            // Get the game from the database;
            SQLGameDAO gameDB = new SQLGameDAO();

            // Update game in the database
            gameDB.updateGame(new GameData(game, "Test Game", "tester12", null, 9999));
        });
    }

    @Test
    @Order(8)
    public void updatePersistance() {
        assertDoesNotThrow(() -> {
            ChessGame game = new ChessGame();
            ChessMove move = new ChessMove(new ChessPosition(1, 2), new ChessPosition(3, 3), null);
            game.makeMove(move);
            // Get the game from the database;
            SQLGameDAO gameDB = new SQLGameDAO();
            GameData gameData = gameDB.getGame(1111);

            assertNotNull(gameData, "Game not found in database");
            assertEquals(game, gameData.game(), "Game not updated in database");

            // Make sure that game 2222 was not updated
            GameData gameData2 = gameDB.getGame(2222);
            assertEquals(new ChessGame(), gameData2.game(), "Game 2222 was updated");
        });
    }

    @Test
    @Order(9)
    public void clearGames() {
        assertDoesNotThrow(() -> {
            // Clear the database
            SQLGameDAO gameDB = new SQLGameDAO();
            gameDB.clear();

            Collection<GameData> games = gameDB.listGames();
            assertEquals(0, games.size(), "Database not cleared");
        });
    }
}
