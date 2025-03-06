package dataaccess;

import model.AuthData;

public class SQLAuthDAO implements AuthDAO{
    @Override
    public AuthData getAuth(String authToken) {
        return null;
    }

    @Override
    public void createAuth(AuthData auth) {

    }

    @Override
    public void deleteAuth(AuthData auth) throws DataAccessException {

    }

    @Override
    public void clear() {

    }
}
