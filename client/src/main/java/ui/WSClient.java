package ui;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;
import websocket.commands.UserGameCommand;

import javax.websocket.*;
import java.net.URI;
import java.net.URISyntaxException;

public class WSClient extends Endpoint {

    public Session session;
    private ServerMessageObserver observer;
    private final URI uri;
    private final WebSocketContainer container;

    public WSClient(String serverURL, int port) throws Exception {
        uri = new URI("ws://" + serverURL + ":" + port + "/ws");
        container = ContainerProvider.getWebSocketContainer();

        this.observer = null;

    }

    private void establishConnection() throws Exception{
        this.session = container.connectToServer(this, uri);
        this.session.addMessageHandler(String.class, this::messageReceived);
    }

    public void registerObserver(ServerMessageObserver observer) {
        this.observer = observer;
    }

    private void messageReceived(String message) {
        // Deserialize the message
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ChessGame.class, new ChessGame.ChessGameAdapter());
        gsonBuilder.registerTypeAdapter(ChessGame.class, new ChessGame.ChessGameDeserializer());
        Gson serializer = gsonBuilder.create();

        ServerMessage serverMessage = serializer.fromJson(message, ServerMessage.class);
        if (serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.LOAD_GAME) {
            serverMessage = serializer.fromJson(message, LoadGameMessage.class);
        } else if (serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.NOTIFICATION) {
            serverMessage = serializer.fromJson(message, NotificationMessage.class);
        } else if (serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.ERROR) {
            serverMessage = serializer.fromJson(message, ErrorMessage.class);
        }
        // Notify the observer
        if (observer != null) {
            observer.notify(serverMessage);
        }
    }

    public void send(UserGameCommand command) throws Exception {
        // Serialize the command
        String msg = new Gson().toJson(command);
        // If the session is closed, reconnect
        if (session == null || !session.isOpen()) {
            establishConnection();
        }
        session.getBasicRemote().sendText(msg);
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {

    }
}
