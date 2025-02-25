package server;

import com.google.gson.Gson;
import model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.DatabaseService;
import service.GameService;
import service.ServiceError;
import service.UserService;
import spark.*;

public class Handlers {
    private static final Gson serializer = new Gson();
    private static final Logger log = LoggerFactory.getLogger(Handlers.class);

    public static String clearApplication(Request request, Response response) {
        DatabaseService.clearDatabase();
        return "{}";
    }

    public static String register(Request request, Response response) {
        // Create request object
        RegisterRequest regRequest;
        regRequest = serializer.fromJson(request.body(), RegisterRequest.class);

        // Register the user
        RegisterResult result = UserService.register(regRequest);
        return serializer.toJson(result);
    }

    public static String login(Request request, Response response) {
        // Create request object
        LoginRequest loginRequest;
        loginRequest = serializer.fromJson(request.body(), LoginRequest.class);

        // Login the user
        LoginResult result = UserService.login(loginRequest);
        return serializer.toJson(result);
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
        return serializer.toJson(result);
    }

    public static String createGame(Request request, Response response) {
        // Create request object
        String authToken = request.headers("Authorization");
        NewGameRequest gameRequest = serializer.fromJson(request.body(), NewGameRequest.class);
        gameRequest = new NewGameRequest(authToken, gameRequest.gameName());

        // Create game
        NewGameResult result = GameService.newGame(gameRequest);
        return serializer.toJson(result);
    }

    public static String joinGame(Request request, Response response) {
        // Create request object
        String authToken = request.headers("Authorization");
        JoinGameRequest joinRequest = serializer.fromJson(request.body(), JoinGameRequest.class);
        joinRequest = new JoinGameRequest(authToken, joinRequest.playerColor(), joinRequest.gameID());

        // Join game
        GameService.joinGame(joinRequest);
        return "{}";
    }
}
