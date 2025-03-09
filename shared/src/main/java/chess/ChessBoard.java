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

    public Set<Map.Entry<ChessPosition, ChessPiece>> getEntries() {
        return board.entrySet();
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

    /**
     * Gets all the moves a team can make
     * @param team Team to check
     * @return Collection of all moves the team can make
     */
    public Collection<ChessMove> getTeamMoves(ChessGame.TeamColor team) {
        Collection<ChessMove> moves = new ArrayList<>();

        board.forEach((position, piece) -> {
            if (getPiece(position).getTeamColor() == team) {
                moves.addAll(piece.pieceMoves(this, position));
            }
        });

        return moves;
    }

    /**
     * Gets the position of all the team's pieces
     * @param team Team of interest
     * @return List of piece positions
     */
    public Collection<ChessPosition> getTeamPieceLocations(ChessGame.TeamColor team) {
        Collection<ChessPosition> positions = new ArrayList<>();

        board.forEach((position, piece) -> {
            if (getPiece(position).getTeamColor() == team) {
                positions.add(position);
            }
        });

        return positions;
    }

    /**
     * Gets the position of the team's king
     * @param team Color of king to find
     * @return Position of the king
     */
    public ChessPosition findTeamKing(ChessGame.TeamColor team) {
        for (Map.Entry<ChessPosition, ChessPiece> entry : board.entrySet()) {
            if (entry.getValue().getTeamColor() == team && entry.getValue().getPieceType() == ChessPiece.PieceType.KING) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Removes the piece at the given position
     * @param position target position
     */
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

    /**
     * Creates a deep copy of the chessboard
     * @return copy of board
     */
    public ChessBoard createCopy() {
        ChessBoard copyBoard = new ChessBoard();
        copyBoard.board = (HashMap<ChessPosition, ChessPiece>) board.clone();
        return copyBoard;
    }
}
