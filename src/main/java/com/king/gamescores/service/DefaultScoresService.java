package com.king.gamescores.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * The non-functional requirement to no persistence to disk forces me to create a thread-safe singleton with lazy
 * initialization with double check locking.
 */
public class DefaultScoresService implements ScoresService {

    protected static final int DEFAULT_MAX_NUMBER = 15;
    protected Map<Integer, Map<Integer, Integer>> scoresByLevel;
    protected int maxNumber;

    /**
     * Constructs a {@link DefaultScoresService} with the default maximum number of scores per level
     */
    protected DefaultScoresService() {
        scoresByLevel = new HashMap<>();
        maxNumber = DEFAULT_MAX_NUMBER;
    }

    /**
     * For tests proposes. Constructs a {@link DefaultScoresService} with the given maximum number of scores per level
     *
     * @param maxNumber maximum number of scores per level
     */
    protected DefaultScoresService(int maxNumber) {
        scoresByLevel = new HashMap<>();
        this.maxNumber = maxNumber;
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
            scores = new HashMap<>(maxNumber);
            scores.put(userId, scoreValue);
            scoresByLevel.put(level, scores);
        }
    }

    protected void register(int userId, int scoreValue, Map<Integer, Integer> scores) {
        Integer currentScore = scores.get(userId);
        boolean higherScore = currentScore != null && currentScore.compareTo(scoreValue) < 0;
        if (currentScore == null || higherScore) {
            if (scores.size() >= maxNumber) {
                Entry<Integer, Integer> min = scores.entrySet().stream()
                        .min(Entry.comparingByValue(Integer::compareTo)).get();
                if (min.getValue().compareTo(scoreValue) < 0) {
                    if (currentScore == null) {
                        scores.remove(min.getKey());
                    }
                    scores.put(userId, scoreValue);
                }
            } else {
                scores.put(userId, scoreValue);
            }
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
        Map<Integer, Integer> scoreMap = scoresByLevel.getOrDefault(level, new HashMap<>(0));
        Map<Integer, Integer> highestScores = scoreMap.entrySet().stream()
                .sorted(Collections.reverseOrder(Entry.comparingByValue()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue,
                        (e1, e2) -> e2, () -> new LinkedHashMap<>(scoreMap.size())));
        List<String> results = new ArrayList<>(highestScores.size());
        highestScores.forEach((k, v) -> results.add(String.format("%s=%s", k, v)));
        return String.join(",", results);
    }
}
