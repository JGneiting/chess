package chess;


/**
 * SpecialMove class implements the log
 */
public class SpecialMove extends ChessMove {
    MoveType type;
    MoveSide side;

    public SpecialMove(ChessPosition startPosition, MoveType type, MoveSide side) {
        super(startPosition, generateDestination(startPosition, type, side), null);
        this.type = type;
        this.side = side;
    }

    /**
     * Generates the destination for the move based on its type
     * @param start Starting position for the move
     * @param type Type of move to perform
     * @param side Which side the move is performed on
     * @return Destination for the given move
     */
    static ChessPosition generateDestination(ChessPosition start, MoveType type, MoveSide side) {
        int direction = side == MoveSide.LEFT ? -1 : 1;
        if (type == MoveType.CASTLE) {
            return start.applyOffset(0, 2 * direction);
        } else if (type == MoveType.EN_PASSANT) {
            return start.applyOffset(start.getRow() == 5 ? 1 : -1, direction);
        } else {
            return start.applyOffset((start.getRow() == 2 ? 2 : -2), 0);
        }
    }

    public enum MoveType {
        CASTLE,
        EN_PASSANT,
        DOUBLE_MOVE
    }

    public enum MoveSide {
        LEFT,
        RIGHT
    }

    /**
     * Executes the given move on the chessboard
     * @param board Board to modify
     */
    public void executeMove(ChessBoard board) {
        switch (type) {
            case CASTLE -> castle(board);
            case EN_PASSANT -> enPassant(board);
            case DOUBLE_MOVE -> doubleMove(board);
        }
    }

    /**
     * Performs the castling move
     * @param board Board to modify
     */
    private void castle(ChessBoard board) {
        // Piece in reference is the king, will move two in the direction of the castle
        int direction = side == MoveSide.LEFT ? -1 : 1;
        ChessPosition rookStart = new ChessPosition(super.getStartPosition().getRow(), side == MoveSide.LEFT ? 1 : 8);
        ChessPosition rookDestination = super.getEndPosition().applyOffset(0, direction*-1);

        // Move the rook
        board.addPiece(rookDestination, board.getPiece(rookStart));
        board.removePiece(rookStart);
    }

    /**
     * Deletes the pawn that en passant captures
     * @param board Board to modify
     */
    private void enPassant(ChessBoard board) {
        // Piece in reference is the attacking pawn
        int direction = side == MoveSide.LEFT ? -1 : 1;

        // Remove the pawn that was captured
        board.removePiece(super.getStartPosition().applyOffset(0, direction));
    }

    /**
     * Sets the double move flag on a pawn
     * @param board Board to modify
     */
    private void doubleMove(ChessBoard board) {
        board.getPiece(getEndPosition()).setDoubleMove(true);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
