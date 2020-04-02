package com.king.gamescores.data;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public final class ScoresData {

    private static final int DEFAULT_MAX_NUMBER = 15;

    private static ScoresData instance = null;

    private final Map<Integer, Map<Integer, Integer>> scores;
    private final int maxNumber;

    private ScoresData() {
        scores = new ConcurrentHashMap<>();
        maxNumber = DEFAULT_MAX_NUMBER;
    }

    private ScoresData(int maxNumber) {
        scores = new ConcurrentHashMap<>();
        this.maxNumber = maxNumber;
    }

    public static ScoresData getInstance() {
        if (instance == null) {
            synchronized (ScoresData.class) {
                if (instance == null) {
                    instance = new ScoresData();
                }
            }
        }
        return instance;
    }

    /**
     * Only for test purposes
     *
     * @param maxNumber maximum number of scores per level
     * @return ScoresData
     */
    public static ScoresData getInstance(int maxNumber) {
        return new ScoresData(maxNumber);
    }

    public void registerScore(int level, int userId, int score) {
        Map<Integer, Integer> scoreMap = scores.get(level);
        if (scoreMap != null) {
            if (scoreMap.size() >= maxNumber) {
                Map.Entry<Integer, Integer> min = scoreMap.entrySet().stream()
                        .min(Map.Entry.comparingByValue(Integer::compareTo)).get();
                scoreMap.remove(min.getKey());
            }
            Integer currentScore = scoreMap.get(userId);
            if (currentScore != null) {
                if (score > currentScore) {
                    scoreMap.put(userId, score);
                }
            } else {
                scoreMap.put(userId, score);
            }
        } else {
            scoreMap = new ConcurrentHashMap<>(maxNumber);
            scoreMap.put(userId, score);
            scores.put(level, scoreMap);
        }
    }

    public String getHighScoresForLevel(int level) {
        SortedMap<Integer, Integer> highestScores = new TreeMap<>(Collections.reverseOrder());
        Map<Integer, Integer> scoreMap = scores.get(level);
        if (scoreMap != null) {
            scoreMap.forEach((key, value) -> highestScores.put(value, key));
        }
        StringBuilder sb = new StringBuilder();
        highestScores.forEach((key, value) -> sb.append(String.format("%s=%s,", value, key)));
        String result = sb.toString();
        return result.length() > 0 ? result.substring(0, result.length() - 1) : result;
    }
}
