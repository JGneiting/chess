package server;

import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;

import java.util.Collection;
import java.util.HashMap;

public class WSSHandlers {

    private static HashMap<Integer, Collection<Session>> subscriptionLists;

    private static void sendErrorMessage(Session session, String message) {
        // Create an error message object
        ErrorMessage errorMessage = new ErrorMessage(message);
        // Send the error message to the client
        WSServer.sendMessage(session, errorMessage);
    }

    public static void parseCommand(UserGameCommand command, Session session) {
        // Handle the command based on its type
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
    }

    private static void handleConnect(UserGameCommand command, Session session) {

    }

    private static void handleMakeMove(UserGameCommand command, Session session) {

    }

    private static void handleLeave(UserGameCommand command, Session session) {

    }

    private static void handleResign(UserGameCommand command, Session session) {

    }
}
