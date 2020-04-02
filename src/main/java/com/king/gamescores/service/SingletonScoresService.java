package com.king.gamescores.service;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The non-functional requirement to no persistence to disk forces me to create a thread-safe singleton with lazy
 * initialization with double check locking.
 */
public class SingletonScoresService implements ScoresService {

    private static final int DEFAULT_MAX_NUMBER = 15;

    private static SingletonScoresService instance = null;

    protected final Map<Integer, Map<Integer, Integer>> scores;
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
        Map<Integer, Integer> scoreMap = scores.get(level);
        if (scoreMap != null) {
            if (scoreMap.size() >= maxNumber) {
                // Because of memory reasons no more than maxNumber scores are to be registered for each level. Find
                // the minimum score and remove it
                Map.Entry<Integer, Integer> min = scoreMap.entrySet().stream()
                        .min(Map.Entry.comparingByValue(Integer::compareTo)).get();
                scoreMap.remove(min.getKey());
            }
            Integer currentScore = scoreMap.get(userId);
            if (currentScore != null) {
                if (score > currentScore) {
                    // Store only the highest user score
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
        // key=score, value=userId in reverse order (highest scores first) as the result must be a comma separated
        // list in descending score order
        SortedMap<Integer, Integer> highestScores = new TreeMap<>(Collections.reverseOrder());
        // Retrieve the key=userId, value=score per level
        Map<Integer, Integer> scoreMap = scores.get(level);
        if (scoreMap != null) {
            scoreMap.forEach((key, value) -> highestScores.put(value, key));
        }
        // build CSV of <userid>=<score>
        StringBuilder sb = new StringBuilder();
        highestScores.forEach((key, value) ->
                // Note that here we are switching the value(userid) and the key(score)
                sb.append(String.format("%s=%s,", value, key))
        );
        String result = sb.toString();
        // remove the last comma if applies
        return result.length() > 0 ? result.substring(0, result.length() - 1) : result;
    }

}
