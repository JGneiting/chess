package dataaccess;

import model.GameData;
import java.util.Collection;

public interface GameDAO {
    Collection<GameData> listGames();
    void createGame(GameData game);
    GameData getGame(String gameID);
    void updateGame(GameData game) throws DataAccessException;
    void clear();
}
