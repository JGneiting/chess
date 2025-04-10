package ui;

import chess.*;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import static ui.EscapeSequences.*;
import static ui.EscapeSequences.RESET_TEXT_COLOR;

public class GameplayUI implements ServerMessageObserver {

    private final String gameplay =
            String.format("""
            %sredraw%s - Redraw the board
            %sleave%s - Leave the game
            %smove <FROM> <TO> [QUEEN|ROOK|BISHOP|KNIGHT]%s - Move piece from FROM to TO, upgrading to indicated piece if applicable
            %sresign%s - Resign from the game
            %shighlight <POSITION>%s - Highlight valid moves for piece at POSITION
            %shelp%s - List available commands%s""",
                    SET_TEXT_COLOR_GREEN, SET_TEXT_COLOR_BLUE, SET_TEXT_COLOR_GREEN, SET_TEXT_COLOR_BLUE,
                    SET_TEXT_COLOR_GREEN, SET_TEXT_COLOR_BLUE, SET_TEXT_COLOR_GREEN, SET_TEXT_COLOR_BLUE,
                    SET_TEXT_COLOR_GREEN, SET_TEXT_COLOR_BLUE, SET_TEXT_COLOR_GREEN, SET_TEXT_COLOR_BLUE, RESET_TEXT_COLOR);

    private static final String BOARD_BORDER_COLOR = SET_BG_COLOR_DARK_GREEN + SET_TEXT_COLOR_LIGHT_GREY;
    private static final String BOARD_LIGHT_SQUARE_COLOR = SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_LIGHT_GREY;
    private static final String BOARD_DARK_SQUARE_COLOR = SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_DARK_GREY;
    private static final String BOARD_LIGHT_HIGHLIGHT_COLOR = SET_BG_COLOR_LIGHT_BLUE + SET_TEXT_COLOR_LIGHT_BLUE;
    private static final String BOARD_DARK_HIGHLIGHT_COLOR = SET_BG_COLOR_DARK_BLUE + SET_TEXT_COLOR_DARK_BLUE;
    private static final String BOARD_LIGHT_SQUARE_HIGHLIGHT_COLOR = SET_BG_COLOR_LIGHT_BLUE + SET_TEXT_COLOR_BLACK;
    private static final String BOARD_DARK_SQUARE_HIGHLIGHT_COLOR = SET_BG_COLOR_DARK_BLUE + SET_TEXT_COLOR_BLACK;
    private static final String BOARD_LIGHT_SQUARE_SELECT_COLOR = SET_BG_COLOR_LIGHT_YELLOW + SET_TEXT_COLOR_BLACK;
    private static final String BOARD_DARK_SQUARE_SELECT_COLOR = SET_BG_COLOR_DARK_YELLOW + SET_TEXT_COLOR_BLACK;
    private static final String WHITE_PIECE_COLOR = SET_TEXT_COLOR_WHITE;
    private static final String BLACK_PIECE_COLOR = SET_TEXT_COLOR_BLACK;

    private final String[] rowLabels = {
            "A", "B", "C", "D", "E", "F", "G", "H"
    };

    private final String[] columnLabels = {
            "8", "7", "6", "5", "4", "3", "2", "1"
    };

    private final HashMap<String, ChessPiece.PieceType> pieceUpgradeMap = new HashMap<>() {{
        put("QUEEN", ChessPiece.PieceType.QUEEN);
        put("BISHOP", ChessPiece.PieceType.BISHOP);
        put("KNIGHT", ChessPiece.PieceType.KNIGHT);
        put("ROOK", ChessPiece.PieceType.ROOK);
    }};

    private final HashMap<ChessPiece.PieceType, String> pieceMap = new HashMap<>() {{
        put(ChessPiece.PieceType.KING, BLACK_KING);
        put(ChessPiece.PieceType.QUEEN, BLACK_QUEEN);
        put(ChessPiece.PieceType.BISHOP, BLACK_BISHOP);
        put(ChessPiece.PieceType.KNIGHT, BLACK_KNIGHT);
        put(ChessPiece.PieceType.ROOK, BLACK_ROOK);
        put(ChessPiece.PieceType.PAWN, BLACK_PAWN);
        put(null, EMPTY);
    }};

    private final ServerFacade facade;
    static String role;
    private static ChessGame game;
    private static int gameID;

    public static void setRole(String role) {
        GameplayUI.role = role;
    }

    public static void setGameID(int gameID) {
        GameplayUI.gameID = gameID;
    }

    public static void setGame(ChessGame game) {
        GameplayUI.game = game;
    }

    public GameplayUI(ServerFacade facade) {
        // Set the role for the game
        this.facade = facade;
        this.facade.registerObserver(this);
    }

    public ClientLoop.UIState gameplayOptions(String input) {
        // If game is null, give default board
        if (game == null) {
            game = new ChessGame();
        }
        // Parse the command
        String[] command = ClientLoop.parseCommand(input);
        return switch (command[0]) {
            case "redraw" -> {
                // Expect exactly one argument
                ClientLoop.expectCommandCount(command, 1);
                // Redraw the board
                Collection<String> board = convertBoard();
                drawBoard(board);
                yield ClientLoop.UIState.GAMEPLAY;
            }
            case "leave" -> {
                // Expect exactly one argument
                ClientLoop.expectCommandCount(command, 1);
                // Send leave request
                facade.leaveGame(ClientLoop.getAuthToken(), gameID);
                yield ClientLoop.UIState.LOG_IN;
            }
            case "move" -> {
                // Expect 3-4 arguments
                if (command.length < 3 || command.length > 4) {
                    throw new IllegalArgumentException("Invalid number of arguments. Expected 3 or 4.");
                }
                // If there are four arguments, the last one must be a piece name
                ChessPiece.PieceType upgrade = null;
                if (command.length == 4) {
                    if (!pieceUpgradeMap.containsKey(command[3].toUpperCase())) {
                        throw new IllegalArgumentException("Invalid piece name. Must be one of: QUEEN, ROOK, BISHOP, KNIGHT.");
                    }
                    upgrade = pieceUpgradeMap.get(command[3].toUpperCase());
                }
                // Send move request
                ChessPosition source = convertToPosition(command[1]);
                ChessPosition destination = convertToPosition(command[2]);
                ChessMove move = new ChessMove(source, destination, upgrade);

                facade.makeMove(move, ClientLoop.getAuthToken(), gameID);
                yield ClientLoop.UIState.GAMEPLAY;
            }
            case "resign" -> {
                // Expect exactly one argument
                ClientLoop.expectCommandCount(command, 1);
                // Send resign message
                facade.resignGame(ClientLoop.getAuthToken(), gameID);
                yield ClientLoop.UIState.LOG_IN;
            }
            case "highlight" -> {
                // Expect exactly two arguments
                ClientLoop.expectCommandCount(command, 2);

                ChessPosition selectedPosition = convertToPosition(command[1]);
                // Get the valid moves for the piece at the selected position
                Collection<ChessMove> validMoves = game.validMoves(selectedPosition);
                Collection<ChessPosition> highlightedPositions = new ArrayList<>();
                for (ChessMove move : validMoves) {
                    highlightedPositions.add(move.getEndPosition());
                }
                // Redraw the board
                Collection<String> board = convertBoard(highlightedPositions, selectedPosition);
                drawBoard(board);

                yield ClientLoop.UIState.GAMEPLAY;
            }
            case "help" -> {
                // Expect exactly one argument
                ClientLoop.expectCommandCount(command, 1);
                System.out.println(gameplay);
                yield ClientLoop.UIState.GAMEPLAY;
            }
            default -> throw new IllegalArgumentException("Invalid command. Type 'help' for a list of commands.");
        };
    }

    private ChessPosition convertToPosition(String position) {
        // Uppercase the position
        position = position.toUpperCase();
        // Check the position is valid
        if (!position.matches("[A-H][1-8]")) {
            throw new IllegalArgumentException("Invalid position. Use format <LETTER><NUMBER>.");
        }
        // Convert the position to a ChessPosition object
        int row = position.charAt(1) - '0';
        int column = position.charAt(0) - 'A' + 1;
        return new ChessPosition(row, column);
    }

    private void drawBoard(Collection<String> board) {
        boolean white = !role.equals("BLACK");
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
    }

    private static void displayLabelRow(String[] viewRowLabels) {
        System.out.printf("%s%s    %s    %s%s%s%n",
                BOARD_BORDER_COLOR, PIXEL,
                String.join(PIXEL.repeat(5), viewRowLabels),
                PIXEL, RESET_BG_COLOR, RESET_TEXT_COLOR
        );
    }

    private Collection<String> convertBoard() {
        Collection<ChessPosition> highlights = new ArrayList<>();
        return convertBoard(highlights, null);
    }

    private Collection<String> convertBoard(Collection<ChessPosition> highlightedPositions, ChessPosition selectedPosition) {
        Collection<String> board = new ArrayList<>();
        boolean white = !role.equals("BLACK");

        ChessBoard gameBoard = game.getBoard();
        for (int i = white ? 1 : 8; white ? i < 9 : i > 0; i += white ? 1 : -1) {
            StringBuilder row = new StringBuilder();
            for (int j = white ? 1 : 8; white ? j < 9 : j > 0; j+= white ? 1 : -1) {
                ChessPiece piece = gameBoard.getPiece(new ChessPosition(9-i, j));
                boolean highlight = false;
                for (ChessPosition pos : highlightedPositions) {
                    if (pos.equals(new ChessPosition(9-i, j))) {
                        highlight = true;
                        break;
                    }
                }
                boolean selected = selectedPosition != null && selectedPosition.equals(new ChessPosition(9-i, j));
                if ((i + j) % 2 == 0) {
                    if (highlight)
                        row.append(BOARD_LIGHT_SQUARE_HIGHLIGHT_COLOR);
                    else if (selected)
                        row.append(BOARD_LIGHT_SQUARE_SELECT_COLOR);
                    else
                        row.append(BOARD_LIGHT_SQUARE_COLOR);
                } else {
                    if (highlight)
                        row.append(BOARD_DARK_SQUARE_HIGHLIGHT_COLOR);
                    else if (selected)
                        row.append(BOARD_DARK_SQUARE_SELECT_COLOR);
                    else
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
                        if (highlight)
                            row.append(BOARD_LIGHT_HIGHLIGHT_COLOR);
                        else
                            row.append(BOARD_LIGHT_SQUARE_COLOR);
                    } else {
                        if (highlight)
                            row.append(BOARD_DARK_HIGHLIGHT_COLOR);
                        else
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

    @Override
    public void notify(ServerMessage message) {
        // Determine what kind of message this is
        switch (message.getServerMessageType()) {
            case LOAD_GAME -> {
                // Load the game
                ChessGame loadedGame = ((LoadGameMessage) message).getGame();
                setGame(loadedGame);
                Collection<String> board = convertBoard();
                drawBoard(board);
            }
            case ERROR -> {
                // Handle error message
                System.out.println(SET_TEXT_COLOR_RED + "Error: " + ((ErrorMessage) message).getError() + RESET_TEXT_COLOR);
                // Redraw command prompt

            }
            case NOTIFICATION -> {
                // Handle notification message
                System.out.println(SET_TEXT_COLOR_BLUE + ((NotificationMessage) message).getMessage() + RESET_TEXT_COLOR);
            }
        }
        ClientLoop.displayStateString(ClientLoop.UIState.GAMEPLAY);
    }
}
