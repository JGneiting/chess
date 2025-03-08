package dataaccess;

import model.UserData;

interface UserDAO {
    UserData getUser(String username) throws DataAccessException;
    void createUser(UserData user) throws DataAccessException;
    void clear() throws DataAccessException;
}
