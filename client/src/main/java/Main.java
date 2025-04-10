import chess.*;
import ui.ClientLoop;

public class Main {

    public static void main(String[] args) throws Exception {
        // args[0] should be the server URL
        ClientLoop runner = new ClientLoop(args[0], Integer.parseInt(args[1]));

        runner.run();

        System.out.println("Goodbye!");
    }


}