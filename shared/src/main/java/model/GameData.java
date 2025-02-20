package model;

import chess.ChessGame;


public record GameData(ChessGame game, String gameName, String blackUsername, String whiteUsername, String gameID) {}
