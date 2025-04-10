package server;

import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import spark.Session;
import spark.Spark;

@WebSocket
public class WSServer {

    public static void run(int port) {
        // Start the WebSocket server
        Spark.port(port);
        Spark.webSocket("/ws", WSServer.class);
        Spark.get("/echo/:msg", (req, res) -> "HTTP response: " + req.params(":msg"));
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        // Handle incoming WebSocket messages here
        System.out.println("Received message: " + message);
        // You can send a response back to the client if needed
        // session.getRemote().sendString("Response message");
    }
}
