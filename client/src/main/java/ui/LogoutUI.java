package ui;

import model.LoginRequest;
import model.LoginResult;
import model.RegisterRequest;
import model.RegisterResult;
import ui.ClientLoop;

import static ui.EscapeSequences.*;

public class LogoutUI {
    private final String noauth =
            String.format("""
            %sregister <USERNAME> <PASSWORD> <EMAIL>%s - Register a new account
            %slogin <USERNAME> <PASSWORD>%s - Log in to an existing account
            %squit%s - Exit the program
            %shelp%s - List available commands%s""",
                    SET_TEXT_COLOR_GREEN, SET_TEXT_COLOR_BLUE, SET_TEXT_COLOR_GREEN, SET_TEXT_COLOR_BLUE,
                    SET_TEXT_COLOR_GREEN, SET_TEXT_COLOR_BLUE, SET_TEXT_COLOR_GREEN, SET_TEXT_COLOR_BLUE,
                    RESET_TEXT_COLOR);

    public ClientLoop.UIState logoutOptions(String in) {
        String[] command = ClientLoop.parseCommand(in);
        return switch (command[0]) {
            case "register" :
                // We expect exactly 4 arguments
                ClientLoop.expectCommandCount(command, 4);

                // Send the register request
                RegisterRequest request = new RegisterRequest(command[1], command[2], command[3]);
                RegisterResult regResult = ClientLoop.facade.register(request);

                ClientLoop.setAuthToken(regResult.authToken());
                System.out.println(SET_TEXT_COLOR_BLUE + "Logged in as " + regResult.username() + RESET_TEXT_COLOR);
                yield ClientLoop.UIState.LOG_IN;
            case "login":
                // We expect exactly 3 arguments
                ClientLoop.expectCommandCount(command, 3);

                // Send login request
                LoginRequest loginRequest = new LoginRequest(command[1], command[2]);
                LoginResult logResult = ClientLoop.facade.login(loginRequest);
                // Save the authToken
                ClientLoop.setAuthToken(logResult.authToken());
                System.out.println(SET_TEXT_COLOR_BLUE + "Logged in as " + logResult.username() + RESET_TEXT_COLOR);
                yield ClientLoop.UIState.LOG_IN;
            case "quit":
                // Expect exactly one argument
                ClientLoop.expectCommandCount(command, 1);

                yield ClientLoop.UIState.QUIT;
            case "help":
                // Expect exactly one argument
                ClientLoop.expectCommandCount(command, 1);

                System.out.println(noauth);
                yield ClientLoop.UIState.LOG_OUT;
            default:
                throw new IllegalArgumentException("Invalid command. Type 'help' for a list of commands.");
        };
    }
}
