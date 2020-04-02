package com.king.gamescores.service;

public interface ScoresService {

    void registerScore(int level, int userId, int score);

    String getHighScoresForLevel(int level);
}
