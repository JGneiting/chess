package service;

import dataaccess.*;

public class DatabaseService {
    static SQLUserDAO userDB;

    static {
        try {
            userDB = new SQLUserDAO();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static SQLGameDAO gameDB;

    static {
        try {
            gameDB = new SQLGameDAO();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static SQLAuthDAO authDB;

    static {
        try {
            authDB = new SQLAuthDAO();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void clearDatabase() throws DataAccessException {
        userDB.clear();
        gameDB.clear();
        authDB.clear();
    }
}
