package dataaccess;

import model.GameData;

import java.util.ArrayList;
import java.util.Collection;

public class MemoryGameDAO implements GameDAO{

    static Collection<GameData> games = new ArrayList<>();

    @Override
    public Collection<GameData> listGames() {
        return games;
    }

    @Override
    public void createGame(GameData game) {
        games.add(game);
    }

    @Override
    public GameData getGame(int gameID) {
        GameData locatedGame = null;

        // Try to find game in list
        for (GameData game : games) {
            if (game.gameID() == gameID) {
                locatedGame = game;
                break;
            }
        }

        return locatedGame;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        // Find the game matching this game's gameID
        GameData gameToUpdate = getGame(game.gameID());

        // Throw exception if we did not find it
        if (gameToUpdate == null) {
            throw new DataAccessException("Game does not exist");
        }

        // Remove the game we found from the database, and add the updated version
        games.remove(gameToUpdate);
        createGame(game);
    }

    @Override
    public void clear() {
        games.clear();
    }
}
