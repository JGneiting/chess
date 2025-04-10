package ui;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import model.*;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class ServerFacade {

    private final HTTPClient http;
    private final WSClient ws;

    public ServerFacade(String serverURL, int httpPort, int wsPort) throws Exception {
        this.http = new HTTPClient(serverURL, httpPort);
        this.ws = new WSClient(serverURL, wsPort);
    }

    public void registerObserver(ServerMessageObserver observer) {
        ws.registerObserver(observer);
    }

    public void clearApplication() throws ResponseException {
        http.makeRequest("DELETE", "/db", null, null);
    }

    public RegisterResult register(RegisterRequest request) throws ResponseException {
        var path = "/user";
        return http.makeRequest("POST", path, request, RegisterResult.class);
    }

    public LoginResult login(LoginRequest request) throws ResponseException {
        var path = "/session";
        return http.makeRequest("POST", path, request, LoginResult.class);
    }

    public void logout(LogoutRequest request) throws ResponseException {
        var path = "/session";
        http.makeRequest("DELETE", path, request, null);
    }

    public ListGamesResult listGames(ListGamesRequest request) throws ResponseException {
        var path = "/game";
        return http.makeRequest("GET", path, request, ListGamesResult.class);
    }

    public NewGameResult createGame(NewGameRequest request) throws ResponseException {
        var path = "/game";
        return http.makeRequest("POST", path, request, NewGameResult.class);
    }

    public void joinGame(JoinGameRequest request) throws ResponseException {
        var path = "/game";
        http.makeRequest("PUT", path, request, null);
    }

    public void connectWS(String authToken, int gameID) {
         // send CONNECT message to the websocket
        UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
        try {
            ws.send(command);
        } catch (Exception e) {
            throw new RuntimeException("Error sending command to server: " + e.getMessage());
        }
    }

    public void makeMove(ChessMove move, String authToken, int gameID) {
        // send MAKE_MOVE message to the websocket
        MakeMoveCommand command = new MakeMoveCommand(authToken, gameID, move);
        try {
            ws.send(command);
        } catch (Exception e) {
            throw new RuntimeException("Error sending command to server");
        }
    }

    public void leaveGame(String authToken, int gameID) {
        // send LEAVE message to the websocket
        UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
        try {
            ws.send(command);
        } catch (Exception e) {
            throw new RuntimeException("Error sending command to server");
        }
    }

    public void resignGame(String authToken, int gameID) {
        // send RESIGN message to the websocket
        UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
        try {
            ws.send(command);
        } catch (Exception e) {
            throw new RuntimeException("Error sending command to server");
        }
    }
}
