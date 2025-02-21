package dataaccess;

import model.AuthData;

import java.util.ArrayList;
import java.util.Collection;

public class MemoryAuthDAO implements AuthDAO{
    static Collection<AuthData> auths = new ArrayList<>();

    @Override
    public AuthData getAuth(String authToken) {
        AuthData locatedAuth = null;

        // Find auth data in the auths list
        for (AuthData auth : auths) {
            if (auth.authToken().equals(authToken)) {
                locatedAuth = auth;
                break;
            }
        }

        return locatedAuth;
    }

    @Override
    public void createAuth(AuthData auth) {
        auths.add(auth);
    }

    @Override
    public void deleteAuth(AuthData auth) throws DataAccessException {
        // Throw exception if auth does not exist
        if (!auths.contains(auth)) {
            throw new DataAccessException("Auth does not exist");
        }

        // Delete the auth
        auths.remove(auth);
    }

    @Override
    public void clear() {
        auths.clear();
    }
}
