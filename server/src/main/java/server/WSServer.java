package server;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.Session;
import spark.Spark;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
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
        System.out.println("Serializing message");
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ChessGame.class, new ChessGame.ChessGameAdapter());
        gsonBuilder.registerTypeAdapter(ChessGame.class, new ChessGame.ChessGameDeserializer());
        Gson serializer = gsonBuilder.create();
        String msg = serializer.toJson(message);
        System.out.println("Serialized message: " + msg);
        // Send message to the client.
        try {
            session.getRemote().sendString(msg);
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
            throw new RuntimeException("Error sending message to client", e);
        }
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        // Deserialize the message
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        if (command.getCommandType() == UserGameCommand.CommandType.MAKE_MOVE) {
            command = new Gson().fromJson(message, MakeMoveCommand.class);
        }

        // Pass the command to the command parser
        WSHandlers.parseCommand(command, session);
    }

    @OnWebSocketError
    public void onError(Session session, Throwable throwable) {
        // Handle WebSocket errors
        System.out.println("WebSocket error: " + throwable.getMessage());
    }
}
