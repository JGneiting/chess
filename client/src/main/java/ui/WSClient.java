package ui;

import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.net.URI;
import java.net.URISyntaxException;

public class WSClient extends Endpoint {

    public Session session;
    private final ServerMessageObserver observer;

    public WSClient(int port, ServerMessageObserver observer) throws Exception {
        URI uri = new URI("ws://localhost:" + port + "/ws");
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, uri);
        this.observer = observer;
        this.session.addMessageHandler(String.class, this::messageReceived);
    }

    private void messageReceived(String message) {
        // Convert message to serverMessage
        ServerMessage serverMessage = new ServerMessage();
        // Notify the observer
        observer.notify();
    }

    public void send(String msg) throws Exception {
        session.getBasicRemote().sendText(msg);
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {

    }
}
