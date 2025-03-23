package ui;

import model.LoginRequest;
import model.RegisterRequest;

import static ui.EscapeSequences.*;

public class ClientLoop {
    private enum UIState {
        LOG_OUT,
        LOG_IN,
        GAMEPLAY,
        QUIT
    }

    private final String noauth =
            String.format("""
            %sregister <USERNAME> <PASSWORD> <EMAIL>%s - Register a new account
            %slogin <USERNAME> <PASSWORD>%s - Log in to an existing account
            %squit%s - Exit the program
            %shelp%s - List available commands%s""",
            SET_TEXT_COLOR_GREEN, SET_TEXT_COLOR_BLUE, SET_TEXT_COLOR_GREEN, SET_TEXT_COLOR_BLUE,
            SET_TEXT_COLOR_GREEN, SET_TEXT_COLOR_BLUE, SET_TEXT_COLOR_GREEN, SET_TEXT_COLOR_BLUE,
            RESET_TEXT_COLOR);

    private final String auth =
            String.format("""
            %screate <NAME>%s - Create a game on the server
            %slist%s - List available games
            %sjoin <ID> [WHITE|BLACK]%s - Join a game
            %sobserve <ID>%s - Observe a game
            %slogout%s - Log out of account
            %squit%s - Exit the program
            %shelp%s - List available commands%s""",
            SET_TEXT_COLOR_GREEN, SET_TEXT_COLOR_BLUE, SET_TEXT_COLOR_GREEN, SET_TEXT_COLOR_BLUE,
            SET_TEXT_COLOR_GREEN, SET_TEXT_COLOR_BLUE, SET_TEXT_COLOR_GREEN, SET_TEXT_COLOR_BLUE,
            SET_TEXT_COLOR_GREEN, SET_TEXT_COLOR_BLUE, SET_TEXT_COLOR_GREEN, SET_TEXT_COLOR_BLUE,
            SET_TEXT_COLOR_GREEN, SET_TEXT_COLOR_BLUE, RESET_TEXT_COLOR);

    ServerFacade facade;

    private String[] parseCommand(String in) {
        return in.split(" ");
    }

    public ClientLoop(String serverUrl) {
        facade = new ServerFacade(serverUrl);
    }

    public void run() {
        // Listen loop
        String input = "";
        var state = UIState.LOG_OUT;
        while (state != UIState.QUIT) {
            displayStateString(state);
            input = System.console().readLine();
            try {
                state = switch (state) {
                    case LOG_OUT:
                        yield logoutOptions(input);
                    case LOG_IN:
                        yield loginOptions(input);
                    case GAMEPLAY:

                    default:
                        yield state;
                };
            } catch (Exception e) {
                System.out.println(SET_TEXT_COLOR_RED + e.getMessage() + RESET_TEXT_COLOR);
            }
        }
    }

    private void displayStateString(UIState state) {
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

    private UIState logoutOptions(String in) {
        String[] command = parseCommand(in);
        return switch (command[0]) {
            case "register" :
                // We expect exactly 4 arguments
                if (command.length != 4) {
                    throw new IllegalArgumentException("Invalid number of arguments. Expected 4.");
                }

                // Send the register request
                RegisterRequest request = new RegisterRequest(command[1], command[2], command[3]);
                facade.register(request);
                yield UIState.LOG_OUT;
            case "login":
                // We expect exactly 3 arguments
                if (command.length != 3) {
                    throw new IllegalArgumentException("Invalid number of arguments. Expected 3.");
                }

                // Send login request
                LoginRequest loginRequest = new LoginRequest(command[1], command[2]);
                facade.login(loginRequest);
                yield UIState.LOG_IN;
            case "quit":
                // Expect exactly one argument
                if (command.length != 1) {
                    throw new IllegalArgumentException("Invalid number of arguments. Expected 1.");
                }

                yield UIState.QUIT;
            case "help":
                // Expect exactly one argument
                if (command.length != 1) {
                    throw new IllegalArgumentException("Invalid number of arguments. Expected 1.");
                }

                System.out.println(noauth);
                yield UIState.LOG_OUT;
            default:
                System.out.println("Invalid command. Type 'help' for a list of commands.");
                yield UIState.LOG_OUT;
        };
    }

    private UIState loginOptions(String in) {
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
