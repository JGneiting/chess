package server;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.Session;
import spark.Spark;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;

@WebSocket
public class WSServer {

    public static void run(int port) {
        // Start the WebSocket server
        Spark.port(port);
        Spark.webSocket("/ws", WSServer.class);
    }

    public static void sendMessage(Session session, ServerMessage message) {
        // Serialize message
        String msg = new Gson().toJson(message);
        // Send message to the client.
        try {
            session.getRemote().sendString(msg);
        } catch (IOException e) {
            throw new RuntimeException("Error sending message to client", e);
        }
    }

    @OnWebSocketMessage
    public static void onMessage(Session session, String message) {
        // Deserialize the message
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        if (command.getCommandType() == UserGameCommand.CommandType.MAKE_MOVE) {
            command = new Gson().fromJson(message, MakeMoveCommand.class);
        }

        // Pass the command to the command parser
        WSSHandlers.parseCommand(command, session);
    }
}
