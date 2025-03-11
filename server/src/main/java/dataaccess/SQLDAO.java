package dataaccess;

import java.sql.SQLException;

import static dataaccess.DatabaseManager.createDatabase;
import static dataaccess.DatabaseManager.getConnection;

public class SQLDAO {

    public SQLDAO(String createStatement) throws DataAccessException {
        createDatabase();
        try (var conn = getConnection()) {
            try (var preparedStatement = conn.prepareStatement(createStatement)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to create table: %s", ex.getMessage()));
        }
    }

}
