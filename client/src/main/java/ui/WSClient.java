package ui;

import com.google.gson.Gson;
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

    public WSClient(String serverURL, int port) throws Exception {
        URI uri = new URI("ws://" + serverURL + ":" + port + "/ws");
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, uri);
        this.observer = null;
        this.session.addMessageHandler(String.class, this::messageReceived);
    }

    public void registerObserver(ServerMessageObserver observer) {
        this.observer = observer;
    }

    private void messageReceived(String message) {
        // Deserialize the message
        ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);
        if (serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.LOAD_GAME) {
            serverMessage = new Gson().fromJson(message, LoadGameMessage.class);
        } else if (serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.NOTIFICATION) {
            serverMessage = new Gson().fromJson(message, NotificationMessage.class);
        } else if (serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.ERROR) {
            serverMessage = new Gson().fromJson(message, ErrorMessage.class);
        }
        // Notify the observer
        if (observer != null) {
            observer.notify(serverMessage);
        }
    }

    public void send(UserGameCommand command) throws Exception {
        // Serialize the command
        String msg = new Gson().toJson(command);
        session.getBasicRemote().sendText(msg);
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {

    }
}
