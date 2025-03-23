import chess.*;

public class Main {
    private static enum UIState {
        LOG_OUT,
        LOG_IN,
        GAMEPLAY,
        QUIT
    }

    private static final String noauth =
            """
            register <USERNAME> <PASSWORD> <EMAIL> - Register a new account
            login <USERNAME> <PASSWORD> - Log in to an existing account
            quit - Exit the program
            help - List available commands
            """;

    private static final String auth =
            """
            create <NAME> - Create a game on the server
            list - List available games
            join <ID> [WHITE|BLACK] - Join a game
            observe <ID> - Observe a game
            logout - Log out of account
            quit - Exit the program
            help - List available commands
            """;

    private static String[] parseCommand(String in) {
        return in.split(" ");
    }

    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);

        // Listen loop
        String input = "";
        var state = UIState.LOG_OUT;
        while (state != UIState.QUIT) {
            displayStateString(state);
            input = System.console().readLine();
            state = switch (state) {
                case LOG_OUT:
                    yield logoutOptions(input);
                case LOG_IN:
                    yield loginOptions(input);
                case GAMEPLAY:

                default:
                    yield state;
            };
        }
    }

    private static void displayStateString(UIState state) {
        switch (state) {
            case LOG_OUT:
                System.out.print("[LOGGED_OUT] >>> ");
                break;
            case LOG_IN:
                System.out.print("[LOGGED_IN] >>> ");
                break;
            case GAMEPLAY:
                break;
            default:
                System.out.print("Invalid state.");
        }
    }

    private static UIState logoutOptions(String in) {
        String[] command = parseCommand(in);
        return switch (command[0]) {
            case "register" :
                yield UIState.LOG_IN;
            case "login":
                yield UIState.QUIT;
            case "quit":
                yield UIState.QUIT;
            case "help":
                System.out.println(noauth);
                yield UIState.LOG_OUT;
            default:
                System.out.println("Invalid command. Type 'help' for a list of commands.");
                yield UIState.LOG_OUT;
        };
    }

    private static UIState loginOptions(String in) {
        String[] command = parseCommand(in);
        return switch (command[0]) {
            case "create":
                yield UIState.LOG_IN;
            case "list":
                yield UIState.LOG_IN;
            case "join":
                yield UIState.GAMEPLAY;
            case "observe":
                yield UIState.LOG_IN;
            case "logout":
                yield UIState.LOG_OUT;
            case "quit":
                yield UIState.QUIT;
            case "help":
                System.out.println(auth);
                yield UIState.LOG_IN;
            default:
                System.out.println("Invalid command. Type 'help' for a list of commands.");
                yield UIState.LOG_IN;
        };
    }
}