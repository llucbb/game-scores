package com.king.gamescores.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * The non-functional requirement to no persistence to disk forces me to create a thread-safe singleton with lazy
 * initialization with double check locking.
 */
public class SingletonScoresService implements ScoresService {

    private static final int DEFAULT_MAX_NUMBER = 15;

    private static SingletonScoresService instance = null;

    private ConcurrentMap<Integer, ConcurrentMap<Integer, Integer>> scores;
    protected final int maxNumber;

    /**
     * Constructs a {@link SingletonScoresService} with the default maximum number of scores per level
     */
    private SingletonScoresService() {
        scores = new ConcurrentHashMap<>();
        maxNumber = DEFAULT_MAX_NUMBER;
    }

    /**
     * For tests proposes. Constructs a {@link SingletonScoresService} with the given maximum number of scores per
     * level
     *
     * @param maxNumber maximum number of scores per level
     */
    protected SingletonScoresService(int maxNumber) {
        scores = new ConcurrentHashMap<>();
        this.maxNumber = maxNumber;
    }

    /**
     * In this method, getInstance is not synchronized but the block which creates instance is synchronized so that
     * minimum number of threads have to wait and that’s only for first time. <br>
     * <p>
     * Pros:
     * <ul>
     * <li>Lazy initialization is possible.</li>
     * <li>It is thread-safe.</li>
     * <li>Performance overhead gets reduced because of synchronized keyword.</li>
     * </ul>
     * <p>
     * Cons:
     * <p>
     * <ul>
     * <li>First time, it can affect performance.</li>
     *
     * @return the {@link SingletonScoresService} intance
     */
    public static SingletonScoresService getInstance() {
        if (instance == null) {
            synchronized (SingletonScoresService.class) {
                if (instance == null) {
                    instance = new SingletonScoresService();
                }
            }
        }
        return instance;
    }

    /**
     * Registers a user's score to a level. Because of memory reasons no more than maxNumber scores are to be registered
     * for each level. The data structures are hash tables supporting full concurrency of retrievals and high expected
     * concurrency for updates.
     *
     * @param level  level of the score to register, 31 bit unsigned integer number
     * @param userId userId of the score to register, 31 bit unsigned integer number
     * @param score  score to register, 31 bit unsigned integer number
     */
    @Override
    public void registerScore(int level, int userId, int score) {
        ConcurrentMap<Integer, Integer> scoreMap = scores.get(level);
        if (scoreMap != null) {
            Integer currentScore = scoreMap.get(userId);
            if (scoreMap.size() >= maxNumber) {
                if (currentScore == null || currentScore.compareTo(score) < 0) {
                    Map.Entry<Integer, Integer> min = scoreMap.entrySet().stream()
                            .min(Map.Entry.comparingByValue(Integer::compareTo)).get();
                    if (min.getValue().compareTo(score) < 0) {
                        if (currentScore == null) {
                            scoreMap.remove(min.getKey());
                        }
                        scoreMap.put(userId, score);
                    }
                }
            } else if (currentScore != null && currentScore.compareTo(score) < 0) {
                scoreMap.put(userId, score);
            } else if (currentScore == null) {
                scoreMap.put(userId, score);
            }
        } else {
            scoreMap = new ConcurrentHashMap<>(maxNumber);
            scoreMap.put(userId, score);
            scores.put(level, scoreMap);
        }
    }

    /**
     * Retrieves the high scores for a specific level. The result is a comma separated list in descending score order.
     * Only the highest score counts. ie: an userId can only appear at most once in the list. If a user hasn't submitted
     * a score for the level, no score is present for that user. A request for a high score list of a level without any
     * scores submitted will be an empty string.
     *
     * @param level 31 bit unsigned integer number
     * @return CSV of <userid>=<score>
     */
    @Override
    public String getHighScoresForLevel(int level) {
        Map<Integer, Integer> highestScores;
        // Retrieve the key=userId, value=score per level
        Map<Integer, Integer> scoreMap = scores.get(level);
        if (scoreMap != null) {
            highestScores = scoreMap.entrySet().stream()
                    .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                            LinkedHashMap::new));
        } else {
            highestScores = new LinkedHashMap<>();
        }

        // build CSV of <userid>=<score>
        List<String> results = new ArrayList<>(highestScores.size());
        highestScores.forEach((k, v) -> results.add(String.format("%s=%s", k, v)));
        return String.join(",", results);
    }
}
