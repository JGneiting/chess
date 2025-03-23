package ui;

import model.*;

public class ServerFacade {
    int port;

    public ServerFacade(int port) {
        this.port = port;
    }

    public RegisterResult register(RegisterRequest request) throws ResponseException {
        return null;
    }

    public LoginResult login(LoginRequest request) throws ResponseException {
        return null;
    }

    public void logout(LogoutRequest request) throws ResponseException {
    }

    public ListGamesResult listGames(ListGamesRequest request) throws ResponseException {
        return null;
    }

    public NewGameResult createGame(NewGameRequest request) throws ResponseException {
        return null;
    }

    public void joinGame(JoinGameRequest request) throws ResponseException {
    }
}
