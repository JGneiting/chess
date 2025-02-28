package server;

import com.google.gson.Gson;
import model.*;
import service.DatabaseService;
import service.GameService;
import service.UserService;
import spark.*;

public class Handlers {
    private static final Gson SERIALIZER = new Gson();

    public static String clearApplication(Request request, Response response) {
        DatabaseService.clearDatabase();
        return "{}";
    }

    public static String register(Request request, Response response) {
        // Create request object
        RegisterRequest regRequest;
        regRequest = SERIALIZER.fromJson(request.body(), RegisterRequest.class);

        // Register the user
        RegisterResult result = UserService.register(regRequest);
        return SERIALIZER.toJson(result);
    }

    public static String login(Request request, Response response) {
        // Create request object
        LoginRequest loginRequest;
        loginRequest = SERIALIZER.fromJson(request.body(), LoginRequest.class);

        // Login the user
        LoginResult result = UserService.login(loginRequest);
        return SERIALIZER.toJson(result);
    }

    public static String logout(Request request, Response response) {
        // Create request object
        String authToken = request.headers("Authorization");
        LogoutRequest logoutRequest = new LogoutRequest(authToken);

        // Logout the user
        UserService.logout(logoutRequest);
        return "{}";
    }

    public static String listGames(Request request, Response response) {
        // Create request object
        String authToken = request.headers("Authorization");
        ListGamesRequest listRequest = new ListGamesRequest(authToken);

        // List games
        ListGamesResult result = GameService.listGames(listRequest);
        return SERIALIZER.toJson(result);
    }

    public static String createGame(Request request, Response response) {
        // Create request object
        String authToken = request.headers("Authorization");
        NewGameRequest gameRequest = SERIALIZER.fromJson(request.body(), NewGameRequest.class);
        gameRequest = new NewGameRequest(authToken, gameRequest.gameName());

        // Create game
        NewGameResult result = GameService.newGame(gameRequest);
        return SERIALIZER.toJson(result);
    }

    public static String joinGame(Request request, Response response) {
        // Create request object
        String authToken = request.headers("Authorization");
        JoinGameRequest joinRequest = SERIALIZER.fromJson(request.body(), JoinGameRequest.class);
        joinRequest = new JoinGameRequest(authToken, joinRequest.playerColor(), joinRequest.gameID());

        // Join game
        GameService.joinGame(joinRequest);
        return "{}";
    }
}
