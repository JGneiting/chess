import chess.*;
import ui.ClientLoop;

public class Main {

    public static void main(String[] args) {
        ClientLoop runner = new ClientLoop();

        runner.run();

        System.out.println("Goodbye!");
    }


}