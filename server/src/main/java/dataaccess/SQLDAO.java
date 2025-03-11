package dataaccess;

import java.sql.SQLException;

import static dataaccess.DatabaseManager.createDatabase;
import static dataaccess.DatabaseManager.getConnection;

public class SQLDAO {
    private final String[] createStatement = {};

    public SQLDAO() throws DataAccessException {
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

}
