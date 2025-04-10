package ui;

import chess.ChessGame;
import model.*;

import java.util.HashMap;

import static ui.EscapeSequences.*;

public class LoginUI {

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

    HashMap<Integer, Integer> gameMap = new HashMap<>();

    public ClientLoop.UIState loginOptions(String in) {
        String[] command = ClientLoop.parseCommand(in);
        int gameID;
        return switch (command[0]) {
            case "create":
                // Expect at least two arguments
                if (command.length < 2) {
                    throw new IllegalArgumentException("Invalid number of arguments. Expected 2");
                }
                // Join parameters 1 - end to create the game name
                String name = "";
                for (int i = 1; i < command.length; i++) {
                    name += command[i] + " ";
                }

                // Create the game on the server
                NewGameRequest request = new NewGameRequest(ClientLoop.getAuthToken(), name);
                ClientLoop.facade.createGame(request);
                System.out.println(SET_TEXT_COLOR_BLUE + "Created new game: " + name + RESET_TEXT_COLOR);
                yield ClientLoop.UIState.LOG_IN;
            case "list":
                // Expect one argument
                ClientLoop.expectCommandCount(command, 1);
                gameMap.clear();

                // Send list request
                ListGamesRequest listRequest = new ListGamesRequest(ClientLoop.getAuthToken());
                ListGamesResult listResult = ClientLoop.facade.listGames(listRequest);

                // Print the list of games
                displayGameList(listResult);
                yield ClientLoop.UIState.LOG_IN;
            case "join":
                // Expect 3 arguments
                ClientLoop.expectCommandCount(command, 3);
                gameID = validateGameId(command[1]);
                // Third argument must be either "WHITE" or "BLACK"
                if (!command[2].equals("WHITE") && !command[2].equals("BLACK")) {
                    throw new IllegalArgumentException("Invalid player color. Must be either 'WHITE' or 'BLACK'.");
                }

                // Join the game
                JoinGameRequest joinRequest = new JoinGameRequest(ClientLoop.getAuthToken(), command[2], gameID);
                ClientLoop.facade.joinGame(joinRequest);
                GameplayUI.setRole(command[2]);
                GameplayUI.setGameID(gameID);
                ClientLoop.facade.connectWS(ClientLoop.getAuthToken(), gameID);
                yield ClientLoop.UIState.GAMEPLAY;
            case "observe":
                // Expect exactly 2 arguments
                ClientLoop.expectCommandCount(command, 2);
                // Second argument must be a number
                gameID = validateGameId(command[1]);

                // Observe not implemented
//                System.out.println(SET_TEXT_COLOR_RED + "Observe not implemented" + RESET_TEXT_COLOR);
                GameplayUI.setRole("OBSERVER");
                GameplayUI.setGameID(gameID);
                ClientLoop.facade.connectWS(ClientLoop.getAuthToken(), gameID);
                yield ClientLoop.UIState.GAMEPLAY;
            case "logout":
                // Expect exactly one argument
                ClientLoop.expectCommandCount(command, 1);

                // Send logout request
                LogoutRequest logoutRequest = new LogoutRequest(ClientLoop.getAuthToken());
                ClientLoop.facade.logout(logoutRequest);
                System.out.println(SET_TEXT_COLOR_BLUE + "Logged out" + RESET_TEXT_COLOR);

                // Clear the authToken
                ClientLoop.setAuthToken(null);

                yield ClientLoop.UIState.LOG_OUT;
            case "quit":
                // Expect exactly one argument
                ClientLoop.expectCommandCount(command, 1);
                yield ClientLoop.UIState.QUIT;
            case "help":
                // Expect exactly one argument
                ClientLoop.expectCommandCount(command, 1);
                System.out.println(auth);
                yield ClientLoop.UIState.LOG_IN;
            default:
                System.out.println("Invalid command. Type 'help' for a list of commands.");
                yield ClientLoop.UIState.LOG_IN;
        };
    }

    private void displayGameList(ListGamesResult listResult) {
        int i = 1;
        if (listResult.games().length == 0) {
            System.out.println(SET_TEXT_COLOR_RED + "No games available" + RESET_TEXT_COLOR);
            return;
        }
        for (GameData game : listResult.games()) {
            // Associate i with gameID
            gameMap.put(i, game.gameID());

            System.out.printf("%s[%s%s%s]: %sGame Name: %s%s%s%n",
                    SET_TEXT_COLOR_MAGENTA, SET_TEXT_COLOR_YELLOW, i, SET_TEXT_COLOR_MAGENTA, SET_TEXT_BOLD,
                    RESET_TEXT_BOLD_FAINT, game.gameName(), RESET_TEXT_COLOR);

            // Print the players
            String white = game.whiteUsername() == null ? SET_TEXT_COLOR_RED + "None" : game.whiteUsername();
            String black = game.blackUsername() == null ? SET_TEXT_COLOR_RED + "None" : game.blackUsername();
            System.out.printf("    %sWhite:%s %s%s%s%n",
                    SET_TEXT_COLOR_LIGHT_GREY, RESET_TEXT_COLOR, SET_TEXT_COLOR_BLUE, white, RESET_TEXT_COLOR);
            System.out.printf("    %sBlack:%s %s%s%s%n",
                    SET_TEXT_COLOR_LIGHT_GREY, RESET_TEXT_COLOR, SET_TEXT_COLOR_BLUE, black, RESET_TEXT_COLOR);
            i++;
        }
    }

    private int validateGameId(String gameID) {
        // Second argument must be a number
        if (!gameID.matches("\\d+")) {
            throw new IllegalArgumentException("Invalid game ID. Must be a number.");
        }
        // Game ID must be in the gameMap
        if (!gameMap.containsKey(Integer.parseInt(gameID))) {
            throw new IllegalArgumentException("Invalid game ID. Game does not exist.");
        }
        return gameMap.get(Integer.parseInt(gameID));
    }
}
