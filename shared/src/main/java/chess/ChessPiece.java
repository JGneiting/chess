package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private ChessGame.TeamColor pieceColor;
    private PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        return switch (type) {
            case KING -> kingMoves(board, myPosition);
            case QUEEN -> queenMoves(board, myPosition);
            case BISHOP -> bishopMoves(board, myPosition);
            case KNIGHT -> knightMoves(board, myPosition);
            case ROOK -> rookMoves(board, myPosition);
            case PAWN -> pawnMoves(board, myPosition);
        };
    }

    /**
     * @return int representing the direction the piece moves (mainly for pawns)
     */
    private int getDirection() {
        return pieceColor == ChessGame.TeamColor.WHITE ? 1 : -1;
    }

    /**
     * @param piece Piece to check against
     * @return boolean representing if the piece is an enemy piece
     */
    private boolean isEnemyPiece(ChessPiece piece) {
        return piece != null && piece.getTeamColor() != pieceColor;
    }

    /**
     * Calculates pawn moves
     * @param board Chess board to check against
     * @param myPosition Position of the pawn
     * @return Collection of valid moves
     */
    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();
        // If clear, a pawn can move forward one square
        if (board.getPiece(new ChessPosition(myPosition.getRow() + getDirection(), myPosition.getColumn())) == null) {
            moves.add(new ChessMove(myPosition, new ChessPosition(myPosition.getRow() + getDirection(), myPosition.getColumn()), null));
        }

        // If on home row and both squares in front are clear, a pawn can move forward two squares
        int homeRow = pieceColor == ChessGame.TeamColor.WHITE ? 2 : 7;
        if (myPosition.getRow() == homeRow) {
            ChessPosition destination = new ChessPosition(myPosition.getRow() + 2 * getDirection(), myPosition.getColumn());
            if (board.getPiece(new ChessPosition(myPosition.getRow() + getDirection(), myPosition.getColumn())) == null
                    && board.getPiece(destination) == null) {
                moves.add(new ChessMove(myPosition, destination, null));
            }
        }

        // Can move diagonally forward if there is an enemy piece there
        ChessMove target = new ChessMove(myPosition, new ChessPosition(myPosition.getRow() + getDirection(), myPosition.getColumn() - 1), null);
        if (isEnemyPiece(board.getPiece(target.getEndPosition()))) {
            moves.add(target);
        }
        target = new ChessMove(myPosition, new ChessPosition(myPosition.getRow() + getDirection(), myPosition.getColumn() + 1), null);
        if (isEnemyPiece(board.getPiece(target.getEndPosition()))) {
            moves.add(target);
        }

        // Any destination in row 1 or 8 is a promotion. Remove move without promotion and add 4 moves with promotion
        Collection<ChessMove> promotionAccountedMoves = new ArrayList<>();
        for (ChessMove move : moves) {
            if (move.getEndPosition().getRow() == 1 || move.getEndPosition().getRow() == 8) {
                promotionAccountedMoves.add(new ChessMove(move.getStartPosition(), move.getEndPosition(), PieceType.QUEEN));
                promotionAccountedMoves.add(new ChessMove(move.getStartPosition(), move.getEndPosition(), PieceType.ROOK));
                promotionAccountedMoves.add(new ChessMove(move.getStartPosition(), move.getEndPosition(), PieceType.BISHOP));
                promotionAccountedMoves.add(new ChessMove(move.getStartPosition(), move.getEndPosition(), PieceType.KNIGHT));
            } else {
                promotionAccountedMoves.add(move);
            }
        }

        return promotionAccountedMoves;
    }

    /**
     * Calculates rook moves
     * @param board Chess board to check against
     * @param myPosition Position of the rook
     * @return Collection of valid moves
     */
    private Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition myPosition) {
        // Regardless of team, rook moves as far as the board's edge or the first enemy piece along a row or column
        Collection<ChessMove> moves = new ArrayList<>();
        ChessPosition destination;

        // Vertical column
        for (int row = myPosition.getRow() + 1; row <= 8; row++) {
            destination = new ChessPosition(row, myPosition.getColumn());
            ChessPiece piece = board.getPiece(destination);
            if (piece == null) {
                moves.add(new ChessMove(myPosition, destination, null));
            } else {
                if (isEnemyPiece(piece)) {
                    moves.add(new ChessMove(myPosition, destination, null));
                }
                break;
            }
        }

        for (int row = myPosition.getRow() - 1; row >= 1; row--) {
            destination = new ChessPosition(row, myPosition.getColumn());
            ChessPiece piece = board.getPiece(destination);
            if (piece == null) {
                moves.add(new ChessMove(myPosition, destination, null));
            } else {
                if (isEnemyPiece(piece)) {
                    moves.add(new ChessMove(myPosition, destination, null));
                }
                break;
            }
        }

        // Horizontal row
        for (int column = myPosition.getColumn() + 1; column <= 8; column++) {
            destination = new ChessPosition(myPosition.getRow(), column);
            ChessPiece piece = board.getPiece(destination);
            if (piece == null) {
                moves.add(new ChessMove(myPosition, destination, null));
            } else {
                if (isEnemyPiece(piece)) {
                    moves.add(new ChessMove(myPosition, destination, null));
                }
                break;
            }
        }

        for (int column = myPosition.getColumn() - 1; column >= 1; column--) {
            destination = new ChessPosition(myPosition.getRow(), column);
            ChessPiece piece = board.getPiece(destination);
            if (piece == null) {
                moves.add(new ChessMove(myPosition, destination, null));
            } else {
                if (isEnemyPiece(piece)) {
                    moves.add(new ChessMove(myPosition, destination, null));
                }
                break;
            }
        }

        return moves;
    }

    /**
     * Calculates knight moves
     * @param board Chess board to check against
     * @param myPosition Position of the knight
     * @return Collection of valid moves
     */
    private Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();
        int[][] offsets = {
                {2, 1},
                {2, -1},
                {-2, 1},
                {-2, -1},
                {1, 2},
                {1, -2},
                {-1, 2},
                {-1, -2}
        };

        for (int[] offset : offsets) {
            ChessPosition destination = new ChessPosition(myPosition.getRow() + offset[0], myPosition.getColumn() + offset[1]);
            if (!destination.isValid()) {
                continue;
            }
            ChessPiece piece = board.getPiece(destination);
            if (piece == null || isEnemyPiece(piece)) {
                moves.add(new ChessMove(myPosition, destination, null));
            }
        }

        return moves;
    }

    /**
     * Calculates bishop moves
     * @param board Chess board to check against
     * @param myPosition Position of the bishop
     * @return Collection of valid moves
     */
    private Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();

        // Upper right diagonal
        for (int offset = 1; offset < 8; offset++) {
            ChessPosition destination = new ChessPosition(myPosition.getRow() + offset, myPosition.getColumn() + offset);
            if (destination.getRow() > 8 || destination.getColumn() > 8) {
                break;
            }
            ChessPiece piece = board.getPiece(destination);
            if (piece == null) {
                moves.add(new ChessMove(myPosition, destination, null));
            } else {
                if (isEnemyPiece(piece)) {
                    moves.add(new ChessMove(myPosition, destination, null));
                }
                break;
            }
        }

        // Lower right diagonal
        for (int offset = 1; offset < 8; offset++) {
            ChessPosition destination = new ChessPosition(myPosition.getRow() - offset, myPosition.getColumn() + offset);
            if (destination.getRow() < 1 || destination.getColumn() > 8) {
                break;
            }
            ChessPiece piece = board.getPiece(destination);
            if (piece == null) {
                moves.add(new ChessMove(myPosition, destination, null));
            } else {
                if (isEnemyPiece(piece)) {
                    moves.add(new ChessMove(myPosition, destination, null));
                }
                break;
            }
        }

        // Lower left diagonal
        for (int offset = 1; offset < 8; offset++) {
            ChessPosition destination = new ChessPosition(myPosition.getRow() - offset, myPosition.getColumn() - offset);
            if (destination.getRow() < 1 || destination.getColumn() < 1) {
                break;
            }
            ChessPiece piece = board.getPiece(destination);
            if (piece == null) {
                moves.add(new ChessMove(myPosition, destination, null));
            } else {
                if (isEnemyPiece(piece)) {
                    moves.add(new ChessMove(myPosition, destination, null));
                }
                break;
            }
        }

        // Upper left diagonal
        for (int offset = 1; offset < 8; offset++) {
            ChessPosition destination = new ChessPosition(myPosition.getRow() + offset, myPosition.getColumn() - offset);
            if (destination.getRow() > 8 || destination.getColumn() < 1) {
                break;
            }
            ChessPiece piece = board.getPiece(destination);
            if (piece == null) {
                moves.add(new ChessMove(myPosition, destination, null));
            } else {
                if (isEnemyPiece(piece)) {
                    moves.add(new ChessMove(myPosition, destination, null));
                }
                break;
            }
        }

        return moves;
    }

    /**
     * Calculates queen moves
     * @param board Chess board to check against
     * @param myPosition Position of the queen
     * @return Collection of valid moves
     */
    private Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition myPosition) {
        // Queen moves as a bishop and rook combo
        Collection<ChessMove> moves = bishopMoves(board, myPosition);
        moves.addAll(rookMoves(board, myPosition));

        return moves;
    }

    /**
     * Calculates king moves
     * @param board Chess board to check against
     * @param myPosition Position of the king
     * @return Collection of valid moves
     */
    private Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition myPosition) {
        // King can move one square in any direction, so long it is empty or an enemy piece is there
        Collection<ChessMove> moves = new ArrayList<>();
        for (int rx = -1; rx <= 1; rx++) {
            for (int cx = -1; cx <= 1; cx++) {
                if (rx == 0 && cx == 0) {
                    continue;
                }

                ChessPosition destination = new ChessPosition(myPosition.getRow() + rx, myPosition.getColumn() + cx);
                if (!destination.isValid()) {
                    continue;
                }
                ChessPiece piece = board.getPiece(destination);
                if (piece == null || isEnemyPiece(piece)) {
                    moves.add(new ChessMove(myPosition, destination, null));
                }
            }
        }

        return moves;
    }
}
