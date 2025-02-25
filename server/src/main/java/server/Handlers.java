package server;

import com.google.gson.Gson;
import service.DatabaseService;
import service.ServiceError;
import spark.*;

public class Handlers {
    private static final Gson serializer = new Gson();

    public static String clearApplication(Request request, Response response) {
        DatabaseService.clearDatabase();
        return "{}";
    }

    public static String register(Request request, Response response) {
        throw new ServiceError("Not implemented", 500);
    }

    public static String login(Request request, Response response) {
        throw new ServiceError("Not implemented", 500);
    }

    public static String logout(Request request, Response response) {
        throw new ServiceError("Not implemented", 500);
    }

    public static String listGames(Request request, Response response) {
        throw new ServiceError("Not implemented", 500);
    }

    public static String createGame(Request request, Response response) {
        throw new ServiceError("Not implemented", 500);
    }

    public static String joinGame(Request request, Response response) {
        throw new ServiceError("Not implemented", 500);
    }
}
