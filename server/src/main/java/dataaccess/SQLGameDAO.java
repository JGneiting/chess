package dataaccess;

import chess.ChessGame;
import com.google.gson.GsonBuilder;
import model.AuthData;
import model.GameData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import com.google.gson.Gson;
import java.util.List;

import static dataaccess.DatabaseManager.createDatabase;
import static dataaccess.DatabaseManager.getConnection;

public class SQLGameDAO implements GameDAO{

    private final String[] createStatement =  {
            """
            CREATE TABLE IF NOT EXISTS game (
                `json` TEXT DEFAULT NULL,
                `name` varchar(32) NOT NULL,
                `blackUsername` varchar(32) DEFAULT NULL,
                `whiteUsername` varchar(32) DEFAULT NULL,
                `gameId` int NOT NULL,
                PRIMARY KEY (`gameId`)
            )
            """
    };

    public SQLGameDAO() throws DataAccessException {
        createDatabase();
        try (var conn = getConnection()) {
            for (var statement : createStatement) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to create table: %s", ex.getMessage()));
        }
    }

    private GameData readGame(ResultSet rs) throws SQLException {
        String json = rs.getString("json");
        String name = rs.getString("name");
        String white = rs.getString("whiteUsername");
        String black = rs.getString("blackUsername");
        int gameId = rs.getInt("gameId");
        ChessGame game = new Gson().fromJson(json, ChessGame.class);
        return new GameData(game, name, black, white, gameId);
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        var result = new ArrayList<GameData>();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM game";
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(readGame(rs));
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Unable to read data: %s", e.getMessage()));
        }
        return result;
    }

    @Override
    public void createGame(GameData game) throws DataAccessException{
        try (var conn = getConnection()) {
            var statement = "INSERT INTO game (json, name, blackUsername, whiteUsername, gameId) VALUES (?, ?, ?, ?, ?)";
            try (var ps = conn.prepareStatement(statement)) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.registerTypeAdapter(ChessGame.class, new ChessGame.ChessGameAdapter());
                Gson gson = gsonBuilder.create();
                var json = gson.toJson(game.game());
                ps.setString(1, json);
                ps.setString(2, game.gameName());
                ps.setString(3, game.blackUsername());
                ps.setString(4, game.whiteUsername());
                ps.setInt(5, game.gameID());
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to create game: %s", ex.getMessage()));
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException{
        try (var conn = getConnection()) {
            var statement = "SELECT * FROM game where gameId=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameID);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readGame(rs);
                    }
                    return null;
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to read game: %s", ex.getMessage()));
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        try (var conn = getConnection()) {
            var statement = "UPDATE game SET json=?, whiteUsername=?, blackUsername=? WHERE gameId=?";
            try (var ps = conn.prepareStatement(statement)) {
                var json = new Gson().toJson(game.game());
                ps.setString(1, json);
                ps.setString(2, game.whiteUsername());
                ps.setString(3, game.blackUsername());
                ps.setInt(4, game.gameID());
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to update game: %s", ex.getMessage()));
        }
    }

    @Override
    public void clear() throws DataAccessException{
        try (var conn = getConnection()) {
            var statement = "TRUNCATE game";
            try (var ps = conn.prepareStatement(statement)) {
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to clear game: %s", ex.getMessage()));
        }
    }
}
