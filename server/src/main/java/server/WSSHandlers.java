package server;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.SQLAuthDAO;
import dataaccess.SQLGameDAO;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.eclipse.jetty.websocket.api.Session;
import service.GameService;
import websocket.commands.UserGameCommand;
import websocket.messages.*;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class WSSHandlers {

    private static final HashMap<Integer, Collection<Session>> subscriptionLists = new HashMap<>();

    private static void sendErrorMessage(Session session, String message) {
        // Create an error message object
        ErrorMessage errorMessage = new ErrorMessage(message);
        // Send the error message to the client
        WSServer.sendMessage(session, errorMessage);
    }

    private static void notifySubscribers(int gameID, ServerMessage message, Session currSess, boolean includeSelf) {
        // Get the list of subscribers for the game
        Collection<Session> sessions = subscriptionLists.get(gameID);
        if (sessions != null) {
            for (Session session : sessions) {
                // If includeSelf is false, skip the current session
                if (!includeSelf && session.equals(currSess)) {
                    continue;
                }
                WSServer.sendMessage(session, message);
            }
        }
    }

    public static void parseCommand(UserGameCommand command, Session session) {
        // Handle the command based on its type
        try {
            switch (command.getCommandType()) {
                case CONNECT:
                    handleConnect(command, session);
                    break;
                case MAKE_MOVE:
                    handleMakeMove(command, session);
                    break;
                case LEAVE:
                    handleLeave(command, session);
                    break;
                case RESIGN:
                    handleResign(command, session);
                    break;
                default:
                    sendErrorMessage(session, "Unknown command type");
            }
        } catch (DataAccessException e) {
            // Send error message to the client
            sendErrorMessage(session, e.getMessage());
        }
    }

    private static void handleConnect(UserGameCommand command, Session session) throws DataAccessException {
        // Add the user to the subscription list for the game
        int gameID = command.getGameID();
        addSubscription(session, gameID);

        // We need to determine if this user is a player or an observer. To do this, see if their user is assigned
        // to the game. If they are, they are a player. If not, they are an observer.
        SQLGameDAO gameDatabase = new SQLGameDAO();
        GameData gameData = gameDatabase.getGame(gameID);
        String whitePlayer = gameData.whiteUsername();
        String blackPlayer = gameData.blackUsername();

        String username = getUsername(command);

        // Determine if the user has connected as white, black, or observer
        String userType;
        if (username.equals(whitePlayer)) {
            userType = "WHITE";
        } else if (username.equals(blackPlayer)) {
            userType = "BLACK";
        } else {
            userType = "an observer";
        }

        // Send a LOAD_GAME message to the user
        LoadGameMessage loadMessage = new LoadGameMessage(gameData.game());
        System.out.println("Sending LOAD_GAME message to " + username);
        WSServer.sendMessage(session, loadMessage);

        // Send a message to other subscribers that this user has connected
        String message = username + " has connected as " + userType;
        ServerMessage serverMessage = new NotificationMessage(message);
        System.out.println("Sending NOTIFICATION message to other subscribers: " + message);
        notifySubscribers(gameID, serverMessage, session, false);
    }

    private static void addSubscription(Session session, int gameID) {
        if (!subscriptionLists.containsKey(gameID)) {
            subscriptionLists.put(gameID, new ArrayList<>());
        }

        Collection<Session> sessions = subscriptionLists.get(gameID);
        sessions.add(session);
    }

    private static String getUsername(UserGameCommand command) throws DataAccessException {
        // Get the user's username from the auth database
        SQLAuthDAO authDatabase = new SQLAuthDAO();
        AuthData auth = authDatabase.getAuth(command.getAuthToken());
        String username = auth.username();
        return username;
    }

    private static void handleMakeMove(UserGameCommand command, Session session) {

    }

    private static void handleLeave(UserGameCommand command, Session session) {

    }

    private static void handleResign(UserGameCommand command, Session session) {

    }
}
