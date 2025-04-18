package dataaccess;

import model.UserData;

import java.sql.SQLException;

import static dataaccess.DatabaseManager.getConnection;

public class SQLUserDAO extends SQLDAO implements UserDAO{

    private final static String CREATE_STATEMENT =
            """
            CREATE TABLE IF NOT EXISTS user (
                `username` varchar(32) NOT NULL,
                `email` varchar(64) NOT NULL,
                `password` varchar(256) NOT NULL,
                PRIMARY KEY (`username`)
            )
            """;

    public SQLUserDAO() throws DataAccessException {
        super(CREATE_STATEMENT);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException{
        try (var conn = getConnection()) {
            var statement = "SELECT username, email, password FROM user where username=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1,username);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new UserData(username, rs.getString("password"), rs.getString("email"));
                    }
                    return null;
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to get user: %s", ex.getMessage()));
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        try (var conn = getConnection()) {
            var statement = "INSERT INTO user (username, email, password) VALUES (?, ?, ?)";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, user.username());
                ps.setString(2, user.email());
                ps.setString(3, user.password());
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to create user: %s", ex.getMessage()));
        }
    }

    @Override
    public void clear() throws DataAccessException{
        try (var conn = getConnection()) {
            var statement = "TRUNCATE user";
            try (var ps = conn.prepareStatement(statement)) {
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to clear: %s", ex.getMessage()));
        }
    }
}
