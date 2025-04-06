package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import static ui.EscapeSequences.*;
import static ui.EscapeSequences.RESET_TEXT_COLOR;

public class GameplayUI {

    private final String gameplay =
            String.format("""
            %sredraw%s - Redraw the board
            %sleave%s - Leave the game
            %smove <FROM> <TO>%s - Move piece from FROM to TO
            %sresign%s - Resign from the game
            %shighlight <POSITION>%s - Highlight valid moves for piece at POSITION
            %shelp%s - List available commands%s""",
                    SET_TEXT_COLOR_GREEN, SET_TEXT_COLOR_BLUE, SET_TEXT_COLOR_GREEN, SET_TEXT_COLOR_BLUE,
                    SET_TEXT_COLOR_GREEN, SET_TEXT_COLOR_BLUE, SET_TEXT_COLOR_GREEN, SET_TEXT_COLOR_BLUE,
                    SET_TEXT_COLOR_GREEN, SET_TEXT_COLOR_BLUE, SET_TEXT_COLOR_GREEN, SET_TEXT_COLOR_BLUE, RESET_TEXT_COLOR);

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

    private final ServerFacade facade;

    public GameplayUI(ServerFacade facade) {
        // Set the role for the game
        this.facade = facade;
    }

    public ClientLoop.UIState runGameplay(String role) {
        // For now, draw a new chessGame in proper orientation
        ChessGame game = new ChessGame();
        boolean white = !role.equals("BLACK");

        // Convert board to list of strings
        Collection<String> board = convertBoard(game, role);

        drawBoard(white, board);

        return ClientLoop.UIState.LOG_IN;
    }

    private void drawBoard(boolean white, Collection<String> board) {
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
