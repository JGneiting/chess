package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import model.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import static ui.EscapeSequences.*;

public class ClientLoop {
    private enum UIState {
        LOG_OUT,
        LOG_IN,
        GAMEPLAY,
        QUIT
    }

    private String authToken;
    ServerFacade facade;
    HashMap<Integer, Integer> gameMap = new HashMap<>();
    String joinRole;

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

    private static final String BOARD_BORDER_COLOR = SET_BG_COLOR_DARK_GREEN + SET_TEXT_COLOR_LIGHT_GREY;
    private static final String BOARD_LIGHT_SQUARE_COLOR = SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_LIGHT_GREY;
    private static final String BOARD_DARK_SQUARE_COLOR = SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_DARK_GREY;
    private static final String WHITE_PIECE_COLOR = SET_TEXT_COLOR_WHITE;
    private static final String BLACK_PIECE_COLOR = SET_TEXT_COLOR_BLACK;

    private final String[] rowLabels = {
        "A", "B", "C", "D", "E", "F", "G", "H"
    };

    private final String[] columnLabels = {
        "8", "7", "6", "5", "4", "3", "2", "1"
    };

    private final HashMap<ChessPiece.PieceType, String> pieceMap = new HashMap<>() {{
        put(ChessPiece.PieceType.KING, BLACK_KING);
        put(ChessPiece.PieceType.QUEEN, BLACK_QUEEN);
        put(ChessPiece.PieceType.BISHOP, BLACK_BISHOP);
        put(ChessPiece.PieceType.KNIGHT, BLACK_KNIGHT);
        put(ChessPiece.PieceType.ROOK, BLACK_ROOK);
        put(ChessPiece.PieceType.PAWN, BLACK_PAWN);
        put(null, EMPTY);
    }};

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
            if (state != UIState.GAMEPLAY) {
                displayStateString(state);
                input = System.console().readLine();
            }
            try {
                state = switch (state) {
                    case LOG_OUT:
                        yield logoutOptions(input);
                    case LOG_IN:
                        yield loginOptions(input);
                    case GAMEPLAY:
                        yield gameplayOptions(input);
                    default:
                        yield state;
                };
            } catch (Exception e) {
                System.out.println(SET_TEXT_COLOR_RED + e.getMessage() + RESET_TEXT_COLOR);
            }
        }
    }

    private void expectCommandCount(String[] command, int count) {
        if (command.length != count) {
            throw new IllegalArgumentException("Invalid number of arguments. Expected " + count + ".");
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
                expectCommandCount(command, 4);

                // Send the register request
                RegisterRequest request = new RegisterRequest(command[1], command[2], command[3]);
                RegisterResult regResult = facade.register(request);

                authToken = regResult.authToken();
                System.out.println(SET_TEXT_COLOR_BLUE + "Logged in as " + regResult.username() + RESET_TEXT_COLOR);
                yield UIState.LOG_IN;
            case "login":
                // We expect exactly 3 arguments
                expectCommandCount(command, 3);

                // Send login request
                LoginRequest loginRequest = new LoginRequest(command[1], command[2]);
                LoginResult logResult = facade.login(loginRequest);
                // Save the authToken
                authToken = logResult.authToken();
                System.out.println(SET_TEXT_COLOR_BLUE + "Logged in as " + logResult.username() + RESET_TEXT_COLOR);
                yield UIState.LOG_IN;
            case "quit":
                // Expect exactly one argument
                expectCommandCount(command, 1);

                yield UIState.QUIT;
            case "help":
                // Expect exactly one argument
                expectCommandCount(command, 1);

                System.out.println(noauth);
                yield UIState.LOG_OUT;
            default:
                throw new IllegalArgumentException("Invalid command. Type 'help' for a list of commands.");
        };
    }

    private UIState loginOptions(String in) {
        String[] command = parseCommand(in);
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
                NewGameRequest request = new NewGameRequest(authToken, name);
                facade.createGame(request);
                System.out.println(SET_TEXT_COLOR_BLUE + "Created new game: " + name + RESET_TEXT_COLOR);
                yield UIState.LOG_IN;
            case "list":
                // Expect one argument
                expectCommandCount(command, 1);
                gameMap.clear();

                // Send list request
                ListGamesRequest listRequest = new ListGamesRequest(authToken);
                ListGamesResult listResult = facade.listGames(listRequest);

                // Print the list of games
                displayGameList(listResult);
                yield UIState.LOG_IN;
            case "join":
                // Expect 3 arguments
                expectCommandCount(command, 3);
                gameID = validateGameId(command[1]);
                // Third argument must be either "WHITE" or "BLACK"
                if (!command[2].equals("WHITE") && !command[2].equals("BLACK")) {
                    throw new IllegalArgumentException("Invalid player color. Must be either 'WHITE' or 'BLACK'.");
                }

                // Join the game
                JoinGameRequest joinRequest = new JoinGameRequest(authToken, command[2], gameID);
                facade.joinGame(joinRequest);
                joinRole = command[2];
                yield UIState.GAMEPLAY;
            case "observe":
                // Expect exactly 2 arguments
                expectCommandCount(command, 2);
                // Second argument must be a number
                gameID = validateGameId(command[1]);

                // Observe not implemented
//                System.out.println(SET_TEXT_COLOR_RED + "Observe not implemented" + RESET_TEXT_COLOR);
                joinRole = "OBSERVER";

                yield UIState.GAMEPLAY;
            case "logout":
                // Expect exactly one argument
                expectCommandCount(command, 1);

                // Send logout request
                LogoutRequest logoutRequest = new LogoutRequest(authToken);
                facade.logout(logoutRequest);
                System.out.println(SET_TEXT_COLOR_BLUE + "Logged out" + RESET_TEXT_COLOR);

                // Clear the authToken
                authToken = null;

                yield UIState.LOG_OUT;
            case "quit":
                // Expect exactly one argument
                expectCommandCount(command, 1);
                yield UIState.QUIT;
            case "help":
                // Expect exactly one argument
                expectCommandCount(command, 1);
                System.out.println(auth);
                yield UIState.LOG_IN;
            default:
                System.out.println("Invalid command. Type 'help' for a list of commands.");
                yield UIState.LOG_IN;
        };
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

    private UIState gameplayOptions(String in) {
        // For now, draw a new chessGame in proper orientation
        ChessGame game = new ChessGame();
        boolean white = !joinRole.equals("BLACK");

        // Convert board to list of strings
        Collection<String> board = convertBoard(game, joinRole);

        // Generate the view row and column label lists
        String[] viewRowLabels;
        String[] viewColumnLabels;
        if (white) {
            viewRowLabels = rowLabels;
            viewColumnLabels = columnLabels;
        } else {
            viewRowLabels = new String[8];
            viewColumnLabels = new String[8];
            for (int i = 0; i < 8; i++) {
                viewRowLabels[i] = rowLabels[7 - i];
                viewColumnLabels[i] = columnLabels[7 - i];
            }
        }

        // Print the board
        displayLabelRow(viewRowLabels);
        int i = 0;
        for (String row : board) {
            System.out.printf("%s %s %s%s %s %s%s%n",
                    BOARD_BORDER_COLOR, viewColumnLabels[i], row,
                    BOARD_BORDER_COLOR, viewColumnLabels[i],
                    RESET_TEXT_COLOR, RESET_BG_COLOR
            );
            i++;
        }
        displayLabelRow(viewRowLabels);

        return UIState.LOG_IN;
    }

    private static void displayLabelRow(String[] viewRowLabels) {
        System.out.printf("%s%s    %s    %s%s%s%n",
                BOARD_BORDER_COLOR, PIXEL,
                String.join(PIXEL.repeat(5), viewRowLabels),
                PIXEL, RESET_BG_COLOR, RESET_TEXT_COLOR
        );
    }

    private Collection<String> convertBoard(ChessGame game, String role) {
        Collection<String> board = new ArrayList<>();
        boolean white = !role.equals("BLACK");

        ChessBoard gameBoard = game.getBoard();
        for (int i = white ? 1 : 8; white ? i < 9 : i > 0; i += white ? 1 : -1) {
            StringBuilder row = new StringBuilder();
            for (int j = white ? 1 : 8; white ? j < 9 : j > 0; j+= white ? 1 : -1) {
                ChessPiece piece = gameBoard.getPiece(new ChessPosition(9-i, j));
                if ((i + j) % 2 == 0) {
                    row.append(BOARD_LIGHT_SQUARE_COLOR);
                } else {
                    row.append(BOARD_DARK_SQUARE_COLOR);
                }

                // Set the color of the piece
                if (piece != null) {
                    if (piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
                        row.append(BLACK_PIECE_COLOR);
                        row.append(pieceMap.get(piece.getPieceType()));
                    } else {
                        row.append(WHITE_PIECE_COLOR);
                        row.append(pieceMap.get(piece.getPieceType()));
                    }
                } else {
                    if ((i + j) % 2 == 0) {
                        row.append(BOARD_LIGHT_SQUARE_COLOR);
                    } else {
                        row.append(BOARD_DARK_SQUARE_COLOR);
                    }
                    row.append(pieceMap.get(ChessPiece.PieceType.PAWN));
                }
            }
            row.append(RESET_BG_COLOR);
            row.append(RESET_TEXT_COLOR);
            board.add(row.toString());
        }

        return board;
    }

}
