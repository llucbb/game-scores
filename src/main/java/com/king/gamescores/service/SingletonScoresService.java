package com.king.gamescores.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The non-functional requirement to no persistence to disk forces me to create a thread-safe singleton with lazy
 * initialization with double check locking.
 */
public class SingletonScoresService extends DefaultScoresService {

    private static SingletonScoresService instance = null;

    private SingletonScoresService() {
        super();
        scoresByLevel = new ConcurrentHashMap<>();
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
     * @return the {@link SingletonScoresService} instance
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
     * @param level  level of the scoreValue to register, 31 bit unsigned integer number
     * @param userId userId of the scoreValue to register, 31 bit unsigned integer number
     * @param score  scoreValue to register, 31 bit unsigned integer number
     */
    @Override
    public void registerScore(int level, int userId, int score) {
        Map<Integer, Integer> scores = scoresByLevel.get(level);
        if (scores != null) {
            register(userId, score, scores);
        } else {
            scores = new ConcurrentHashMap<>(maxScoresPerLevel);
            scores.put(userId, score);
            scoresByLevel.put(level, scores);
        }
    }
}
