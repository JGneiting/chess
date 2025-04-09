package ui;

import static ui.EscapeSequences.*;

public class ClientLoop {
    public enum UIState {
        LOG_OUT,
        LOG_IN,
        GAMEPLAY,
        QUIT
    }

    private static String authToken;
    static ServerFacade facade;

   public static String getAuthToken() {
        return authToken;
   }

   public static void setAuthToken(String authToken) {
       ClientLoop.authToken = authToken;
   }

    public static String[] parseCommand(String in) {
        return in.split(" ");
    }

    public ClientLoop(String serverUrl) {
        facade = new ServerFacade(serverUrl);
    }

    public void run() {
        LogoutUI logoutUI = new LogoutUI();
        LoginUI loginUI = new LoginUI();
        GameplayUI gameplayUI = new GameplayUI(facade);
        // Listen loop
        String input = "";
        var state = UIState.LOG_OUT;
        while (state != UIState.QUIT) {
//            if (state != UIState.GAMEPLAY) {
                displayStateString(state);
                input = System.console().readLine();
//            }
            try {
                state = switch (state) {
                    case LOG_OUT:
                        yield logoutUI.logoutOptions(input);
                    case LOG_IN:
                        yield loginUI.loginOptions(input);
                    case GAMEPLAY:
                        yield gameplayUI.gameplayOptions(input);
                    default:
                        yield state;
                };
            } catch (Exception e) {
                System.out.println(SET_TEXT_COLOR_RED + e.getMessage() + RESET_TEXT_COLOR);
            }
        }
    }

    public static void expectCommandCount(String[] command, int count) {
        if (command.length != count) {
            throw new IllegalArgumentException("Invalid number of arguments. Expected " + count + ".");
        }
    }

    public static void displayStateString(UIState state) {
        switch (state) {
            case LOG_OUT:
                System.out.print("[LOGGED_OUT] >>> ");
                break;
            case LOG_IN:
                System.out.print("[LOGGED_IN] >>> ");
                break;
            case GAMEPLAY:
                System.out.print("[GAMEPLAY] >>> ");
                break;
            default:
                System.out.print("Invalid state.");
        }
    }











}
