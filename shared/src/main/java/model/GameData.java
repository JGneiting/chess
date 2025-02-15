package model;

import chess.ChessGame;


record GameData(ChessGame game, String gameName, String blackUsername, String whiteUsername, int gameID) {}
