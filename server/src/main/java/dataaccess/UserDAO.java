package dataaccess;

import model.UserData;

interface UserDAO {
    UserData getUser(String username);
    void createUser(UserData user);
    void clear();
}
