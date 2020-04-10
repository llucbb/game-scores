package com.king.gamescores.service;

import com.king.gamescores.log.ScoresLogger;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MultiThreadScoresServiceTest {

    private static final Logger LOG = Logger.getLogger(MultiThreadScoresServiceTest.class.getName());

    static {
        try {
            ScoresLogger.setup();
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private static final int LEVEL = 1;
    private static final int MAX_USERS = 1000;
    private static final int MAX_RESULTS = 15;
    private static final int MAX_THREADS = 1000;
    private static final int MAX_SCORE = 1000000;

    @Test
    public void registerScoreMultithreaded() throws ExecutionException, InterruptedException {
        String result = register(SingletonScoresService.getInstance(MAX_RESULTS));

        Assert.assertNotNull(result);
        Assert.assertFalse(result.isEmpty());
    }

    @Test(expected = ExecutionException.class)
    public void registerScoreNotThreadSafe() throws ExecutionException, InterruptedException {
        String result = register(new DefaultScoresService(MAX_RESULTS));

        Assert.assertNotNull(result);
        Assert.assertFalse(result.isEmpty());
    }

    private String register(ScoresService scoresService) throws InterruptedException, ExecutionException {
        CompletableFuture<Void>[] futures = new CompletableFuture[MAX_THREADS];
        for (int i = 0; i < MAX_THREADS; i++) {
            final int task = i;
            futures[i] = CompletableFuture.runAsync(() -> {
                registerScores(scoresService, task);
                scoresService.getHighScoresForLevel(LEVEL);
            });
        }
        CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(futures);
        try {
            combinedFuture.get();
        } catch (ExecutionException | InterruptedException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            throw e;
        }

        return scoresService.getHighScoresForLevel(LEVEL);

    }

    private void registerScores(ScoresService scoresService, int task) {
        LOG.info("-> start registerScores task " + task);
        for (int i = 0; i < MAX_USERS; i++) {
            scoresService.registerScore(LEVEL,
                    ThreadLocalRandom.current().nextInt(1, MAX_USERS),
                    ThreadLocalRandom.current().nextInt(1, MAX_SCORE));
        }
        LOG.info("<- end registerScores task " + task);
    }
}
