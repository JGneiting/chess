package chess;

import jdk.jshell.spi.ExecutionControl;

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
        Collection<ChessMove> baseMoves = board.getPiece(startPosition).pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();
        TeamColor pieceTeam = board.getPiece(startPosition).getTeamColor();

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

        return validMoves;
    }

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
        if (board.getPiece(move.getStartPosition()) == null ||
            board.getPiece(move.getStartPosition()).getTeamColor() != currentTurn
            ) {
            throw new InvalidMoveException();
        }

        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());
        if (!validMoves.contains(move)) {
            throw new InvalidMoveException();
        }

        // Make move
        makeMoveDirect(move);
        currentTurn = enemyTeam(currentTurn);
    }

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
     * Returns all the valid moves for a given team
     * @param teamColor which team to get the moves for
     * @return Collection of all the valid moves for the team
     */
    public Collection<ChessMove> getAllValidMoves(TeamColor teamColor) {
        Collection<ChessPosition> teamPieces = board.getTeamPieceLocations(teamColor);
        Collection<ChessMove> moves = new ArrayList<>();

        for (ChessPosition position : teamPieces) {
            moves.addAll(validMoves(position));
        }

        return moves;
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
