package dataaccess;

import model.GameData;

public interface GameDAO {
    GameData[] listGames();
    void createGame(GameData game);
    GameData getGame(String gameID);
    void updateGame(GameData game) throws DataAccessException;
    void clear();
}
