package dataaccess;

import model.UserData;

import java.util.ArrayList;
import java.util.Collection;

public class MemoryUserDAO implements UserDAO{

    static Collection<UserData> users = new ArrayList<>();

    @Override
    public UserData getUser(String username) {
        UserData locatedUser = null;

        // Find username in the user array
        for (UserData user : users) {
            if (user.username().equals(username)) {
                locatedUser = user;
                break;
            }
        }
        return locatedUser;
    }

    @Override
    public void createUser(UserData user) {
        users.add(user);
    }

    @Override
    public void clear() {
        users.clear();
    }
}
