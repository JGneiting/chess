package dataaccess;

import model.AuthData;

public interface AuthDAO {
    AuthData getAuth(String authToken);
    void createAuth(AuthData auth);
    void deleteAuth(AuthData auth) throws DataAccessException;
    void clear();
}
