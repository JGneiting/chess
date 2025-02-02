package chess;

import java.util.*;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    private HashMap<ChessPosition, ChessPiece> board;

    public ChessBoard() {
        this.board = new HashMap<>();
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        board.put(position, piece);
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        if (board.containsKey(position)) {
            return board.get(position);
        }
        return null;
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        board.clear();

        // Add white pawns
        for (int i = 1; i <= 8; i++) {
            board.put(new ChessPosition(2, i), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
        }
        // Add black pawns
        for (int i = 1; i <= 8; i++) {
            board.put(new ChessPosition(7, i), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
        }

        // Finish adding white row and black row
        ChessPiece.PieceType[] row = {
                ChessPiece.PieceType.ROOK,
                ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.QUEEN,
                ChessPiece.PieceType.KING,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.ROOK
        };
        for (int i = 1; i <= 8; i++) {
            board.put(new ChessPosition(1, i), new ChessPiece(ChessGame.TeamColor.WHITE, row[i - 1]));
            board.put(new ChessPosition(8, i), new ChessPiece(ChessGame.TeamColor.BLACK, row[i - 1]));
        }
    }

    public Collection<ChessMove> getTeamMoves(ChessGame.TeamColor team) {
        Collection<ChessMove> moves = new ArrayList<>();

        board.forEach((position, piece) -> {
            if (getPiece(position).getTeamColor() == team) {
                moves.addAll(piece.pieceMoves(this, position));
            }
        });

        return moves;
    }

    public Collection<ChessPosition> getTeamPieceLocations(ChessGame.TeamColor team) {
        Collection<ChessPosition> positions = new ArrayList<>();

        board.forEach((position, piece) -> {
            if (getPiece(position).getTeamColor() == team) {
                positions.add(position);
            }
        });

        return positions;
    }

    public ChessPosition findTeamKing(ChessGame.TeamColor team) {
        for (Map.Entry<ChessPosition, ChessPiece> entry : board.entrySet()) {
            if (entry.getValue().getTeamColor() == team && entry.getValue().getPieceType() == ChessPiece.PieceType.KING) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void removePiece(ChessPosition position) {
        board.remove(position);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.equals(board, that.board);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(board);
    }

    public ChessBoard createCopy() {
        ChessBoard copyBoard = new ChessBoard();
        copyBoard.board = (HashMap<ChessPosition, ChessPiece>) board.clone();
        return copyBoard;
    }
}
