package ui;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import model.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class ServerFacade {
    private final String serverUrl;

    public ServerFacade(int port) {
        this.serverUrl = "http://localhost:" + port;
    }

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        }
        catch (ResponseException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseException(500, "Error communicating with server");
        }
    }

    private boolean isSuccessful(int status) {
        return status == 200;
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            try (InputStream respErr = http.getErrorStream()) {
                if (respErr != null) {
                    if (status != 500) {
                        String err = new String(respErr.readAllBytes());
                        // err is JSON, get the message element
                        JsonObject jsonObject = new Gson().fromJson(err, JsonObject.class);
                        throw new ResponseException(status, jsonObject.get("message").getAsString());
                    }
                }
            }

            throw new ResponseException(status, "Error processing request");
        }
    }

    private void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            // If there is an authToken property in request, add it to the header
            boolean body = true;
            if (reqData.contains("authToken")) {
                JsonObject jsonObject = new Gson().fromJson(reqData, JsonObject.class);
                http.addRequestProperty("Authorization", jsonObject.get("authToken").getAsString());
                // If there are no other properties in the request, don't write the body
                if (jsonObject.size() == 1) {
                    body = false;
                }
            }

            if (body) {
                try (OutputStream reqBody = http.getOutputStream()) {
                    reqBody.write(reqData.getBytes());
                }
            }
        }
    }

    private <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    gsonBuilder.registerTypeAdapter(ChessGame.class, new ChessGame.ChessGameAdapter());
                    gsonBuilder.registerTypeAdapter(ChessGame.class, new ChessGame.ChessGameDeserializer());
                    Gson serializer = gsonBuilder.create();
                    response = serializer.fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }

    public void clearApplication() throws ResponseException {
        makeRequest("DELETE", "/db", null, null);
    }

    public RegisterResult register(RegisterRequest request) throws ResponseException {
        var path = "/user";
        return makeRequest("POST", path, request, RegisterResult.class);
    }

    public LoginResult login(LoginRequest request) throws ResponseException {
        var path = "/session";
        return makeRequest("POST", path, request, LoginResult.class);
    }

    public void logout(LogoutRequest request) throws ResponseException {
        var path = "/session";
        makeRequest("DELETE", path, request, null);
    }

    public ListGamesResult listGames(ListGamesRequest request) throws ResponseException {
        var path = "/game";
        return makeRequest("GET", path, request, ListGamesResult.class);
    }

    public NewGameResult createGame(NewGameRequest request) throws ResponseException {
        var path = "/game";
        return makeRequest("POST", path, request, NewGameResult.class);
    }

    public void joinGame(JoinGameRequest request) throws ResponseException {
        var path = "/game";
        makeRequest("PUT", path, request, null);
    }
}
