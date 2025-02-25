package service;

import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;

public class DatabaseService {
    static MemoryUserDAO userDB = new MemoryUserDAO();
    static MemoryGameDAO gameDB = new MemoryGameDAO();
    static MemoryAuthDAO authDB = new MemoryAuthDAO();

    public static void clearDatabase() {
        userDB.clear();
        gameDB.clear();
        authDB.clear();
    }
}
