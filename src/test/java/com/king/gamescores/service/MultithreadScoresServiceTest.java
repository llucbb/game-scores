package com.king.gamescores.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MultithreadScoresServiceTest {

    private ScoresService scoresService;

    @Before
    public void setUp() {
        scoresService = new SingletonScoresService(5);
    }

    @Test
    public void registerScoreMultithreaded() {
        int level = 1;
        scoresService.registerScore(level, 1, 15);
        scoresService.registerScore(level, 2, 4);
        scoresService.registerScore(level, 2, 43);
        scoresService.registerScore(level, 3, 11);
        scoresService.registerScore(level, 4, 2);
        String expectedResult = "2=43,1=15,3=11,4=2";

        String result = scoresService.getHighScoresForLevel(level);

        Assert.assertEquals(expectedResult, result);
    }


}
