package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.SQLGameDAO;
import model.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

public class GameService {
    private final SQLGameDAO gameDB;
    private final UserService userService;

    public GameService() throws DataAccessException {
        userService = new UserService();
        gameDB = new SQLGameDAO();
    }

    public ListGamesResult listGames(ListGamesRequest listRequest) throws ServiceError, DataAccessException {
        // Check authentication
        userService.checkAuth(listRequest.authToken());

        // Get games
        return new ListGamesResult(gameDB.listGames().toArray(new GameData[0]));
    }

    public NewGameResult newGame(NewGameRequest newGameRequest) throws ServiceError, DataAccessException {
        // Check authentication
        userService.checkAuth(newGameRequest.authToken());

        // Check if a valid game name was submitted
        if (newGameRequest.gameName().isEmpty()) {
            throw new ServiceError("Error: bad request", 400);
        }

        // Get a list of current gameIDs to ensure the new game is unique
        Collection<GameData> games = gameDB.listGames();
        Collection<Integer> idList = new ArrayList<>();

        for (GameData game : games) {
            idList.add(game.gameID());
        }
        int gameID = 0;

        Random random = new Random();
        while (gameID == 0 || idList.contains(gameID)) {
            gameID = random.nextInt(9999);
        }

        // Create a new chessgame
        ChessGame newGame = new ChessGame();

        GameData game = new GameData(newGame, newGameRequest.gameName(), null, null, gameID);
        gameDB.createGame(game);

        // Create response
        return new NewGameResult(gameID);
    }

    public void joinGame(JoinGameRequest joinRequest) throws ServiceError, DataAccessException {
        // Check authentication
        AuthData auth = userService.checkAuth(joinRequest.authToken());

        // Check to make sure that the request is valid
        if (joinRequest.playerColor() == null || !(joinRequest.playerColor().equals("WHITE") || joinRequest.playerColor().equals("BLACK"))) {
            throw new ServiceError("Error: bad request", 400);
        }
        GameData game = gameDB.getGame(joinRequest.gameID());

        if (game == null) {
            throw new ServiceError("Error: bad request", 400);
        }

        GameData newGame = doJoinProcess(joinRequest, game, auth);

        // Update the game in the database
        try {
            gameDB.updateGame(newGame);
        } catch (DataAccessException e) {
            throw new ServiceError("Error: " + e.getMessage(), 500);
        }
    }

    private static GameData doJoinProcess(JoinGameRequest joinRequest, GameData game, AuthData auth) throws ServiceError {
        GameData newGame;
        // Check to see if there is a player assigned to the desired team
        if (joinRequest.playerColor().equals("WHITE")) {
            if (game.whiteUsername() == null) {
                // Join the game
                newGame = new GameData(game.game(), game.gameName(), game.blackUsername(), auth.username(), game.gameID());
            } else {
                throw new ServiceError("Error: already taken", 403);
            }
        } else {
            if (game.blackUsername() == null) {
                // Join game
                newGame = new GameData(game.game(), game.gameName(), auth.username(), game.whiteUsername(), game.gameID());
            } else {
                throw new ServiceError("Error: already taken", 403);
            }
        }
        return newGame;
    }
}
