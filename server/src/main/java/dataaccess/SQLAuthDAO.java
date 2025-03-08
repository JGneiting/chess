package dataaccess;

import model.AuthData;

import java.sql.SQLException;

import static dataaccess.DatabaseManager.*;

public class SQLAuthDAO implements AuthDAO{

    private final String[] createStatement =  {
            """
            CREATE TABLE IF NOT EXISTS auth (
                `authToken` varchar(128) NOT NULL,
                `username` varchar(32) NOT NULL,
                PRIMARY KEY (`authToken`)
            )
            """
    };

    public SQLAuthDAO() throws DataAccessException {
        createDatabase();
        try (var conn = getConnection()) {
            for (var statement : createStatement) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not create auth table");
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        try (var conn = getConnection()) {
            var statement = "SELECT username FROM auth where authToken=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new AuthData(authToken, rs.getString("username"));
                    }
                    return null;
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not get auth");
        }
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException{
        try (var conn = getConnection()) {
            var statement = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, auth.authToken());
                ps.setString(2, auth.username());
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not create auth");
        }
    }

    @Override
    public void deleteAuth(AuthData auth) throws DataAccessException {
        try (var conn = getConnection()) {
            var statement = "DELETE FROM auth where authToken=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, auth.authToken());
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not delete auth");
        }
    }

    @Override
    public void clear() throws DataAccessException{
        try (var conn = getConnection()) {
            var statement = "TRUNCATE auth";
            try (var ps = conn.prepareStatement(statement)) {
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not clear auth table");
        }
    }
}
