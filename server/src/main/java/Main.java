import chess.*;
import server.Server;
import server.WSServer;

public class Main {
    public static void main(String[] args) {
        Server server = new Server();
        WSServer.run(Integer.parseInt(args[0]));;
        var port = server.run(Integer.parseInt(args[0]));

        System.out.println("Started test HTTP server on " + port);

        // Stay alive until user stops program
        try {
            while (true) {
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            System.out.println("Server stopped.");
        }
    }
}