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

    private SingletonScoresService(int maxNumber) {
        super(maxNumber);
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
     * @return the {@link SingletonScoresService} intance
     */
    public static SingletonScoresService getInstance(int maxNumber) {
        if (instance == null) {
            synchronized (SingletonScoresService.class) {
                if (instance == null) {
                    instance = new SingletonScoresService(maxNumber);
                }
            }
        }
        return instance;
    }

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
        Map<Integer, Integer> scores = scoresByLevel.get(level);
        if (scores != null) {
            register(userId, scoreValue, scores);
        } else {
            scores = new ConcurrentHashMap<>(maxNumber);
            scores.put(userId, scoreValue);
            scoresByLevel.put(level, scores);
        }
    }
}
