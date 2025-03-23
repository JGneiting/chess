package server;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dataaccess.DataAccessException;
import model.*;
import service.DatabaseService;
import service.GameService;
import service.UserService;
import spark.*;

public class Handlers {
    private static Gson SERIALIZER;

    static {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ChessGame.class, new ChessGame.ChessGameAdapter());
        gsonBuilder.registerTypeAdapter(ChessGame.class, new ChessGame.ChessGameDeserializer());
        SERIALIZER = gsonBuilder.create();
    }

    public static String clearApplication(Request request, Response response) throws DataAccessException {
        DatabaseService.clearDatabase();
        return "{}";
    }

    public static String register(Request request, Response response) throws DataAccessException {
        // Create request object
        RegisterRequest regRequest;
        regRequest = SERIALIZER.fromJson(request.body(), RegisterRequest.class);

        // Register the user
        RegisterResult result = new UserService().register(regRequest);
        return SERIALIZER.toJson(result);
    }

    public static String login(Request request, Response response) throws DataAccessException {
        // Create request object
        LoginRequest loginRequest;
        loginRequest = SERIALIZER.fromJson(request.body(), LoginRequest.class);

        // Login the user
        LoginResult result = new UserService().login(loginRequest);
        return SERIALIZER.toJson(result);
    }

    public static String logout(Request request, Response response) throws DataAccessException {
        // Create request object
        String authToken = request.headers("Authorization");
        LogoutRequest logoutRequest = new LogoutRequest(authToken);

        // Logout the user
        new UserService().logout(logoutRequest);
        return "{}";
    }

    public static String listGames(Request request, Response response) throws DataAccessException {
        // Create request object
        String authToken = request.headers("Authorization");
        ListGamesRequest listRequest = new ListGamesRequest(authToken);

        // List games
        ListGamesResult result = new GameService().listGames(listRequest);
        return SERIALIZER.toJson(result);
    }

    public static String createGame(Request request, Response response) throws DataAccessException {
        // Create request object
        String authToken = request.headers("Authorization");
        NewGameRequest gameRequest = SERIALIZER.fromJson(request.body(), NewGameRequest.class);
        gameRequest = new NewGameRequest(authToken, gameRequest.gameName());

        // Create game
        NewGameResult result = new GameService().newGame(gameRequest);
        return SERIALIZER.toJson(result);
    }

    public static String joinGame(Request request, Response response) throws DataAccessException {
        // Create request object
        String authToken = request.headers("Authorization");
        JoinGameRequest joinRequest = SERIALIZER.fromJson(request.body(), JoinGameRequest.class);
        joinRequest = new JoinGameRequest(authToken, joinRequest.playerColor(), joinRequest.gameID());

        // Join game
        new GameService().joinGame(joinRequest);
        return "{}";
    }
}
