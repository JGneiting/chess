package server;

import dataaccess.DataAccessException;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.ServiceError;
import spark.*;

@WebSocket
public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        WSServer.run(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.delete("/db", Handlers::clearApplication);
        Spark.post("/user", Handlers::register);
        Spark.post("/session", Handlers::login);
        Spark.delete("/session", Handlers::logout);
        Spark.get("/game", Handlers::listGames);
        Spark.post("/game", Handlers::createGame);
        Spark.put("/game", Handlers::joinGame);

        // Handle Service error exception
        Spark.exception(ServiceError.class, (exception, request, response) -> {
            response.status(exception.getCode());
            response.type("application/json");
            response.body("{\"message\": \"" + exception.getMessage() + "\"}");
        });

        // Handle DataAccess error exception
        Spark.exception(DataAccessException.class, (exception, request, response) -> {
            response.status(500);
            response.type("application/json");
            response.body("{\"message\": \"" + exception.getMessage() + "\"}");
        });

        //This line initializes the server and can be removed once you have a functioning endpoint 
//        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
