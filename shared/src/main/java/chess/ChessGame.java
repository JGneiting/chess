package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    TeamColor currentTurn;
    ChessBoard board;

    public ChessGame() {
        currentTurn = TeamColor.WHITE;
        board = new ChessBoard();
        board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currentTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        // A move is valid if moving that piece will not result in the king being in check
        ChessPiece piece = board.getPiece(startPosition);
        Collection<ChessMove> baseMoves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();
        TeamColor pieceTeam = piece.getTeamColor();

        // Save a copy of the board
        ChessBoard startingBoard = board.createCopy();
        for (ChessMove testMove : baseMoves) {
            // Simulate the move
            makeMoveDirect(testMove);
            if (!isInCheck(pieceTeam)) {
                validMoves.add(testMove);
            }
            // Reset the board
            setBoard(startingBoard.createCopy());
        }

        // Check for specialty moves
        checkCastling(startPosition, piece, pieceTeam, validMoves);
        checkEnPassant(startPosition, piece, pieceTeam, validMoves);

        return validMoves;
    }

    /**
     * Adds the castling special moves if the required conditions are met
     * @param startPosition Position of target piece
     * @param piece Piece of interest
     * @param pieceTeam Piece's team color
     * @param validMoves Moves this piece can make
     */
    private void checkCastling(ChessPosition startPosition, ChessPiece piece, TeamColor pieceTeam, Collection<ChessMove> validMoves) {
        // If we are a king, check if we can castle
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            SpecialMove.MoveSide[] sides = {
                    SpecialMove.MoveSide.LEFT,
                    SpecialMove.MoveSide.RIGHT
            };
            for (SpecialMove.MoveSide side : sides) {
                int direction = side == SpecialMove.MoveSide.LEFT ? -1 : 1;
                ChessPiece rook = board.getPiece(new ChessPosition(startPosition.getRow(), side == SpecialMove.MoveSide.LEFT ? 1 : 8));
                // To castle right, the king must not have moved, as well as the rook in the same row in that direction
                if (piece.hasNotMoved() && rook != null && rook.hasNotMoved()) {
                    // Two squares right must be empty
                    boolean open = true;
                    for (int i = 1; i <= (side == SpecialMove.MoveSide.LEFT ? 3 : 2); i++) {
                        if (board.getPiece(new ChessPosition(startPosition.getRow(), startPosition.getColumn() + (direction * i))) != null) {
                            open = false;
                        }
                    }
                    if (!open) {
                        continue;
                    }

                    // We can castle on this side if none of the three squares on the right are targeted by an enemy
                    boolean passesThroughCheck = false;
                    Collection<ChessMove> enemyMoves = board.getTeamMoves(enemyTeam(pieceTeam));
                    Collection<ChessPosition> interestSquares = generateInterestSquares(startPosition, direction);
                    for (ChessMove move : enemyMoves) {
                        if (interestSquares.contains(move.getEndPosition())) {
                            passesThroughCheck = true;
                            break;
                        }
                    }
                    if (!passesThroughCheck) {
                        // The King can castle right
                        validMoves.add(new SpecialMove(startPosition, SpecialMove.MoveType.CASTLE, side));
                    }
                }
            }
        }
    }

    /**
     * Adds the en passant special move if the conditions are met
     * @param startPosition Position of the piece to check
     * @param piece Piece of interest
     * @param pieceTeam Piece's team color
     * @param validMoves Valid moves this piece can perform
     */
    private void checkEnPassant(ChessPosition startPosition, ChessPiece piece, TeamColor pieceTeam, Collection<ChessMove> validMoves) {
        // If we are a pawn with an enemy pawn next to us that has double moved, we can capture it
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            SpecialMove.MoveSide[] sides = {
                    SpecialMove.MoveSide.LEFT,
                    SpecialMove.MoveSide.RIGHT
            };
            for (SpecialMove.MoveSide side : sides) {
                int direction = side == SpecialMove.MoveSide.LEFT ? -1 : 1;
                // Is there a piece in the indicated direction that is an enemy
                ChessPosition targetSquare = startPosition.applyOffset(0, direction);
                ChessPiece targetPiece = board.getPiece(targetSquare);
                if (targetPiece != null && targetPiece.getTeamColor() != pieceTeam && targetPiece.hasDoubleMoved()) {
                    // Add an en passant move in this direction
                    validMoves.add(new SpecialMove(startPosition, SpecialMove.MoveType.EN_PASSANT, side));
                }
            }
        }
    }

    /**
     * Generates the chess positions 3 squares horizontally in the given direction from the reference
     * @param reference Position to offset from
     * @param direction Direction to offset in
     * @return Collection of the three squares of interest
     */
    private Collection<ChessPosition> generateInterestSquares(ChessPosition reference, int direction) {
        Collection<ChessPosition> squares = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            squares.add(new ChessPosition(reference.getRow(), reference.getColumn() + (direction*i)));
        }
        return squares;
    }

    /**
     * Makes a move directly on the chessboard with no checks
     * @param move Move to perform
     */
    private void makeMoveDirect(ChessMove move) {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        board.removePiece(move.getStartPosition());

        if (move.getPromotionPiece() == null) {
            board.addPiece(move.getEndPosition(), piece);
        } else {
            board.addPiece(move.getEndPosition(), new ChessPiece(piece.getTeamColor(), move.getPromotionPiece()));
        }
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        // Check if move is in the valid set of moves for this piece
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece == null ||
            piece.getTeamColor() != currentTurn
            ) {
            throw new InvalidMoveException();
        }

        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());
        if (!validMoves.contains(move)) {
            throw new InvalidMoveException();
        }

        ChessMove matchingMove = null;
        // Grab identical move from valid moves
        for (ChessMove trueMove : validMoves) {
            if (move.equals(trueMove)) {
                matchingMove = trueMove;
                break;
            }
        }

        // Clear all of our team's double move flags
        Collection<ChessPosition> positions = board.getTeamPieceLocations(currentTurn);
        for (ChessPosition pieceLocation : positions) {
            ChessPiece target = board.getPiece(pieceLocation);
            target.setDoubleMove(false);
        }

        // Make move
        piece.setPieceMoved();
        makeMoveDirect(move);
        currentTurn = enemyTeam(currentTurn);
        if (matchingMove instanceof SpecialMove) {
            ((SpecialMove) matchingMove).executeMove(board);
        }
    }

    /**
     * Returns the enemy team's color
     * @param team team to invert
     * @return enemy team color
     */
    private TeamColor enemyTeam(TeamColor team) {
        return team == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        // A team is in check if any enemy piece is able to move onto its square
        ChessPosition kingPosition = board.findTeamKing(teamColor);
        Collection<ChessMove> enemyMoves = board.getTeamMoves(enemyTeam(teamColor));

        // If the king's position is in the destination of any enemy move, we are in check
        for (ChessMove move : enemyMoves) {
            if (move.getEndPosition().equals(kingPosition)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        // In order to be in checkmate, the team's king must be in check
        if (!isInCheck(teamColor)) {
            return false;
        }

        // A team is checkmated if they have no valid moves
        return noValidMoves(teamColor);
    }

    /**
     * Returns if the indicated team has any valid moves
     * @param teamColor Team color to check
     * @return true if team has no valid moves
     */
    private boolean noValidMoves(TeamColor teamColor) {
        Collection<ChessPosition> teamPositions = board.getTeamPieceLocations(teamColor);
        for (ChessPosition position : teamPositions) {
            Collection<ChessMove> validMoves = validMoves(position);
            if (!validMoves.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        // In order to be in checkmate, the team's king must be in check
        if (isInCheck(teamColor)) {
            return false;
        }

        // A team is stalemated if they have no valid moves
        return noValidMoves(teamColor);
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
