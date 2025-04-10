package ui;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class HTTPClient {

    private final String serverUrl;

    public HTTPClient(String serverUrl, int port) {
        this.serverUrl = "http://" + serverUrl + ":" + port;
    }

    public <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) {
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
}
