package model;

public record JoinGameRequest(String authToken, String playerColor, String gameID) {
}
