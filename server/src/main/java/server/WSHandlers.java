package server;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.InvalidMoveException;
import dataaccess.DataAccessException;
import dataaccess.SQLAuthDAO;
import dataaccess.SQLGameDAO;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class WSHandlers {

    private static final HashMap<Integer, Collection<Session>> SUBSCRIPTION_LISTS = new HashMap<>();
    private static final HashMap<Session, String> AUTH_TOKEN_LOOKUP = new HashMap<>();
    private static final HashMap<ChessPiece.PieceType, String> PIECE_NAMES = new HashMap<>() {{
        put(ChessPiece.PieceType.PAWN, "Pawn");
        put(ChessPiece.PieceType.ROOK, "Rook");
        put(ChessPiece.PieceType.KNIGHT, "Knight");
        put(ChessPiece.PieceType.BISHOP, "Bishop");
        put(ChessPiece.PieceType.QUEEN, "Queen");
        put(ChessPiece.PieceType.KING, "King");
    }};
    private static final HashMap<ChessGame.TeamColor, String> TEAM_NAMES = new HashMap<>() {{
        put(ChessGame.TeamColor.WHITE, "White");
        put(ChessGame.TeamColor.BLACK, "Black");
    }};
    private static final String[] COLUMN_NAMES = {"A", "B", "C", "D", "E", "F", "G", "H"};

    private static void sendErrorMessage(Session session, String message) {
        // Create an error message object
        ErrorMessage errorMessage = new ErrorMessage(message);
        // Send the error message to the client
        WSServer.sendMessage(session, errorMessage);
    }

    private static void notifySubscribers(int gameID, ServerMessage message, Session currSess, boolean includeSelf) {
        // Get the list of subscribers for the game
        Collection<Session> sessions = SUBSCRIPTION_LISTS.get(gameID);
        if (sessions != null) {
            for (Session session : sessions) {
                // If includeSelf is false, skip the current session
                if (!includeSelf && session.equals(currSess)) {
                    continue;
                }
                WSServer.sendMessage(session, message);
            }
        }
    }

    public static void parseCommand(UserGameCommand command, Session session) {
        // Is the connection valid?
        if (!session.isOpen()) {
            return;
        }
        // Handle the command based on its type
        try {
            validateCommand(command);
            switch (command.getCommandType()) {
                case CONNECT:
                    handleConnect(command, session);
                    break;
                case MAKE_MOVE:
                    handleMakeMove((MakeMoveCommand) command, session);
                    break;
                case LEAVE:
                    handleLeave(command, session);
                    break;
                case RESIGN:
                    handleResign(command, session);
                    break;
                default:
                    sendErrorMessage(session, "Unknown command type");
            }
        } catch (DataAccessException e) {
            // Send error message to the client
            sendErrorMessage(session, e.getMessage());
        } catch (InvalidCommand e) {
            sendErrorMessage(session, e.getMessage());
        }
    }

    private static void validateCommand(UserGameCommand command) throws InvalidCommand, DataAccessException {
        // Check the gameID
        if (command.getGameID() == null) {
            throw new InvalidCommand("Game ID is required");
        }
        // Check the auth token
        if (command.getAuthToken() == null || command.getAuthToken().isEmpty()) {
            throw new InvalidCommand("Auth token is required");
        }

        // Make sure gameID exists in the database
        SQLGameDAO gameDatabase = new SQLGameDAO();
        GameData gameData = gameDatabase.getGame(command.getGameID());
        if (gameData == null) {
            throw new InvalidCommand("Game ID does not exist");
        }
        // Make sure the authToken exists in the auth database
        SQLAuthDAO authDatabase = new SQLAuthDAO();
        AuthData authData = authDatabase.getAuth(command.getAuthToken());
        if (authData == null) {
            throw new InvalidCommand("Auth token does not exist");
        }
    }

    public static void handleDisconnect(Session session) {
        // Remove the session from all subscription lists
        for (Integer gameID : SUBSCRIPTION_LISTS.keySet()) {
            removeSubscription(session, gameID);
            // For each game the user was subscribed to, mock a leave command to notify others
            UserGameCommand leaveCommand = new UserGameCommand(UserGameCommand.CommandType.LEAVE, AUTH_TOKEN_LOOKUP.get(session), gameID);
            try {
                handleLeave(leaveCommand, session);
            } catch (DataAccessException e) {
                // Do nothing, as the user is already disconnected
            }
        }

        // Remove the session from the auth token lookup
        AUTH_TOKEN_LOOKUP.remove(session);
    }

    private static void addSubscription(Session session, int gameID) {
        if (!SUBSCRIPTION_LISTS.containsKey(gameID)) {
            SUBSCRIPTION_LISTS.put(gameID, new ArrayList<>());
        }

        Collection<Session> sessions = SUBSCRIPTION_LISTS.get(gameID);
        sessions.add(session);
    }

    private static String getUsername(String authToken) throws DataAccessException {
        // Get the user's username from the auth database
        SQLAuthDAO authDatabase = new SQLAuthDAO();
        AuthData auth = authDatabase.getAuth(authToken);
        return auth.username();
    }

    private static ChessGame.TeamColor userTeam(String authToken, int gameID) throws DataAccessException {
        // Get username
        String username = getUsername(authToken);
        // Get game data
        SQLGameDAO gameDatabase = new SQLGameDAO();
        GameData gameData = gameDatabase.getGame(gameID);
        String whitePlayer = gameData.whiteUsername();
        String blackPlayer = gameData.blackUsername();
        // Determine if the user is white, black, or an observer
        if (username.equals(whitePlayer)) {
            return ChessGame.TeamColor.WHITE;
        } else if (username.equals(blackPlayer)) {
            return ChessGame.TeamColor.BLACK;
        } else {
            return null; // Observer
        }
    }

    private static LoadGameMessage generateLoadGameMessage(int gameID) throws DataAccessException {
        // Get the game data
        SQLGameDAO gameDatabase = new SQLGameDAO();
        GameData gameData = gameDatabase.getGame(gameID);
        // Create a LoadGameMessage object
        return new LoadGameMessage(gameData.game());
    }

    private static String convertMoveToText(String username, ChessMove move, ChessGame game) {
        // Get the text representation of the piece at the source square
        ChessPiece piece = game.getBoard().getPiece(move.getEndPosition());
        String pieceName = PIECE_NAMES.get(piece.getPieceType());
        // Get the text representation of the destination square
        String destSquare = COLUMN_NAMES[move.getEndPosition().getColumn()-1] + (move.getEndPosition().getRow());
        // Convert the move to a text representation
        return username + " performed the move " + pieceName + " to " + destSquare;
    }

    private static void handleConnect(UserGameCommand command, Session session) throws DataAccessException {
        // Add the user to the subscription list for the game
        int gameID = command.getGameID();
        String authToken = command.getAuthToken();
        addSubscription(session, gameID);

        // If this user's auth token is not already in the lookup, add it
        if (!AUTH_TOKEN_LOOKUP.containsKey(session)) {
            AUTH_TOKEN_LOOKUP.put(session, authToken);
        }

        // Get the user's role
        ChessGame.TeamColor userTeam = userTeam(authToken, gameID);

        // Determine if the user has connected as white, black, or observer
        String userType;
        if (userTeam == ChessGame.TeamColor.WHITE) {
            userType = "WHITE";
        } else if (userTeam == ChessGame.TeamColor.BLACK) {
            userType = "BLACK";
        } else {
            userType = "an observer";
        }

        String username = getUsername(authToken);
        // Send a LOAD_GAME message to the user
        LoadGameMessage loadMessage = generateLoadGameMessage(gameID);
        System.out.println("Sending LOAD_GAME message to " + username);
        WSServer.sendMessage(session, loadMessage);

        // Send a message to other subscribers that this user has connected
        String message = username + " has connected as " + userType;
        ServerMessage serverMessage = new NotificationMessage(message);
        System.out.println("Sending NOTIFICATION message to other subscribers: " + message);
        notifySubscribers(gameID, serverMessage, session, false);
    }

    private static void handleMakeMove(MakeMoveCommand command, Session session) throws DataAccessException {
        int gameID = command.getGameID();
        // Get user team
        ChessGame.TeamColor userTeamColor = userTeam(command.getAuthToken(), command.getGameID());

        if (userTeamColor == null) {
            sendErrorMessage(session, "You are an observer in this game");
            return;
        }

        ChessMove move = command.getMove();
        // Get the chess game
        SQLGameDAO gameDatabase = new SQLGameDAO();
        GameData gameData = gameDatabase.getGame(command.getGameID());
        ChessGame game = gameData.game();

        // If the game is over, do not allow moves
        if (game.isGameOver()) {
            String winner = TEAM_NAMES.get(game.getWinner());
            if (winner != null) {
                sendErrorMessage(session, winner + " has already won. No more moves allowed.");
            } else {
                sendErrorMessage(session, "Game has ended with a draw. No more moves allowed.");
            }
            return;
        }

        // Verify the user owns the source piece
        ChessPiece target = game.getBoard().getPiece(move.getStartPosition());
        if (target == null) {
            sendErrorMessage(session, "No piece at source position");
            return;
        } else if (target.getTeamColor() != userTeamColor) {
            sendErrorMessage(session, "You do not own the piece at source position");
            return;
        }

        // Make the move
        try {
            game.makeMove(move);
        } catch (InvalidMoveException e) {
            sendErrorMessage(session, e.getMessage());
            return;
        }

        // Update the game data in the database
        gameDatabase.updateGame(gameData);

        // Notify all subscribers to update their boards
        LoadGameMessage loadMessage = generateLoadGameMessage(command.getGameID());
        notifySubscribers(gameID, loadMessage, session, true);

        // Notify other subscribers of the move
        String username = getUsername(command.getAuthToken());
        String message = convertMoveToText(username, move, game);
        ServerMessage serverMessage = new NotificationMessage(message);
        notifySubscribers(gameID, serverMessage, session, false);

        // Check for check, checkmate, or stalemate
        ChessGame.TeamColor enemy = ChessGame.enemyTeam(userTeamColor);
        if (game.isInCheckmate(enemy)) {
            String checkmateMessage = "Checkmate! " + username + " (" + TEAM_NAMES.get(userTeamColor) + ") wins!";
            ServerMessage checkmateNotification = new NotificationMessage(checkmateMessage);
            notifySubscribers(gameID, checkmateNotification, session, true);
        } else if (game.isInStalemate(enemy)) {
            String stalemateMessage = "Stalemate! The game is a draw.";
            ServerMessage stalemateNotification = new NotificationMessage(stalemateMessage);
            notifySubscribers(gameID, stalemateNotification, session, true);
        } else if (game.isInCheck(enemy)) {
            String checkMessage = "Check!";
            ServerMessage checkNotification = new NotificationMessage(checkMessage);
            notifySubscribers(gameID, checkNotification, session, true);
        }
    }

    private static void handleLeave(UserGameCommand command, Session session) throws DataAccessException {
        int gameID = command.getGameID();
        String authToken = command.getAuthToken();
        String username = getUsername(authToken);

        // Get user role
        ChessGame.TeamColor userTeam = userTeam(authToken, gameID);

        // If the user is an observer, just remove them from the subscription list
        if (userTeam != null) {
            // Remove the user from the game
            SQLGameDAO gameDatabase = new SQLGameDAO();
            GameData gameData = gameDatabase.getGame(gameID);
            GameData updatedData;

            if (userTeam == ChessGame.TeamColor.WHITE) {
                updatedData = new GameData(gameData.game(), gameData.gameName(), gameData.blackUsername(), null, gameData.gameID());
            } else {
                updatedData = new GameData(gameData.game(), gameData.gameName(), null, gameData.whiteUsername(), gameData.gameID());
            }

            // Update the game data in the database
            gameDatabase.updateGame(updatedData);
        }

        // Notify other subscribers that the user has left
        String message = username + " has left the game";
        ServerMessage serverMessage = new NotificationMessage(message);
        notifySubscribers(gameID, serverMessage, session, false);

        // Remove the user from the subscription list
        removeSubscription(session, gameID);
    }

    private static void removeSubscription(Session session, int gameID) {
        Collection<Session> sessions = SUBSCRIPTION_LISTS.get(gameID);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                SUBSCRIPTION_LISTS.remove(gameID);
            }
        }
    }

    private static void handleResign(UserGameCommand command, Session session) throws DataAccessException {
        int gameID = command.getGameID();
        String authToken = command.getAuthToken();
        String username = getUsername(authToken);

        // Get user role
        ChessGame.TeamColor userTeam = userTeam(authToken, gameID);
        if (userTeam == null) {
            sendErrorMessage(session, "You are an observer in this game");
            return;
        }

        // Get the game data
        SQLGameDAO gameDatabase = new SQLGameDAO();
        GameData gameData = gameDatabase.getGame(gameID);
        ChessGame game = gameData.game();

        // Can't resign if the game is already over
        if (game.isGameOver()) {
            String winner = TEAM_NAMES.get(game.getWinner());
            if (winner != null) {
                sendErrorMessage(session, winner + " has already won. No more resignations allowed.");
            } else {
                sendErrorMessage(session, "Game has ended with a draw. No more resignations allowed.");
            }
            return;
        }

        // Mark the enemy as the winner and save the game
        game.markWinner(ChessGame.enemyTeam(userTeam));
        gameDatabase.updateGame(gameData);

        // Notify all subscribers of the resignation
        String message = username + " has resigned. " + TEAM_NAMES.get(ChessGame.enemyTeam(userTeam)) + " wins!";
        ServerMessage serverMessage = new NotificationMessage(message);
        notifySubscribers(gameID, serverMessage, session, true);
    }
}
