package com.king.gamescores.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * The non-functional requirement to no persistence to disk forces me to create a thread-safe singleton with lazy
 * initialization with double check locking.
 */
public class SingletonScoresService implements ScoresService {

    private static final int DEFAULT_MAX_NUMBER = 15;

    private static SingletonScoresService instance = null;

    private final ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> scoresByLevel;
    protected final int maxNumber;

    /**
     * Constructs a {@link SingletonScoresService} with the default maximum number of scores per level
     */
    private SingletonScoresService() {
        scoresByLevel = new ConcurrentHashMap<>();
        maxNumber = DEFAULT_MAX_NUMBER;
    }

    /**
     * For tests proposes. Constructs a {@link SingletonScoresService} with the given maximum number of scores per
     * level
     *
     * @param maxNumber maximum number of scores per level
     */
    protected SingletonScoresService(int maxNumber) {
        scoresByLevel = new ConcurrentHashMap<>();
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
     * Registers a user's scoreValue to a level. Because of memory reasons no more than maxNumber scores are to be
     * registered for each level. The data structures are hash tables supporting full concurrency of retrievals and high
     * expected concurrency for updates.
     *
     * @param level      level of the scoreValue to register, 31 bit unsigned integer number
     * @param userId     userId of the scoreValue to register, 31 bit unsigned integer number
     * @param scoreValue scoreValue to register, 31 bit unsigned integer number
     */
    @Override
    public void registerScore(int level, int userId, int scoreValue) {
        ConcurrentHashMap<Integer, Integer> scores = scoresByLevel.putIfAbsent(level,
                new ConcurrentHashMap<>(maxNumber));
        if (scores != null) {
            Integer currentScore = scores.get(userId);
            if (scores.size() >= maxNumber) {
                if (currentScore == null || currentScore.compareTo(scoreValue) < 0) {
                    Map.Entry<Integer, Integer> min = scores.entrySet().stream()
                            .min(Map.Entry.comparingByValue(Integer::compareTo)).get();
                    if (min.getValue().compareTo(scoreValue) < 0) {
                        if (currentScore == null) {
                            scores.remove(min.getKey());
                        }
                        scores.put(userId, scoreValue);
                    }
                }
            } else if (currentScore != null && currentScore.compareTo(scoreValue) < 0) {
                scores.put(userId, scoreValue);
            } else if (currentScore == null) {
                scores.put(userId, scoreValue);
            }
        } else {
            scores = new ConcurrentHashMap<>(maxNumber);
            scores.put(userId, scoreValue);
            scoresByLevel.put(level, scores);
        }
    }

    /**
     * Retrieves the high scores for a specific level. The result is a comma separated list in descending scoreValue
     * order. Only the highest scoreValue counts. ie: an userId can only appear at most once in the list. If a user
     * hasn't submitted a scoreValue for the level, no scoreValue is present for that user. A request for a high
     * scoreValue list of a level without any scores submitted will be an empty string.
     *
     * @param level 31 bit unsigned integer number
     * @return CSV of <userid>=<scoreValue>
     */
    @Override
    public String getHighScoresForLevel(int level) {
        Map<Integer, Integer> highestScores = new LinkedHashMap<>();
        // Retrieve the key=userId, value=score per level
        Map<Integer, Integer> scoreMap = scoresByLevel.get(level);
        if (scoreMap != null) {
            highestScores = scoreMap.entrySet().stream()
                    .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                            LinkedHashMap::new));
        }

        // build CSV of <userid>=<scoreValue>
        List<String> results = new ArrayList<>(highestScores.size());
        highestScores.forEach((k, v) -> results.add(String.format("%s=%s", k, v)));
        return String.join(",", results);
    }
}
