package com.king.gamescores.service;

import com.king.gamescores.util.Strings;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ScoresServiceTest {

    private ScoresService scoresService;

    @Before
    public void setUp() {
        scoresService = new SingletonScoresService(5);
    }

    @Test
    public void emptyScores() {
        int level = 1;

        String result = scoresService.getHighScoresForLevel(level);

        Assert.assertFalse(Strings.isNotEmpty(result));
    }

    @Test
    public void registerLessThanMaxScores() {
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

    @Test
    public void registerMoreThanMaxScores() {
        int level = 1;
        scoresService.registerScore(level, 1, 1);
        scoresService.registerScore(level, 1, 3);
        scoresService.registerScore(level, 2, 2);
        scoresService.registerScore(level, 2, 1);
        scoresService.registerScore(level, 3, 15);
        scoresService.registerScore(level, 4, 13);
        scoresService.registerScore(level, 5, 11);
        scoresService.registerScore(level, 6, 12);
        String expectedResult = "3=15,4=13,6=12,5=11,1=3";

        String result = scoresService.getHighScoresForLevel(level);

        Assert.assertEquals(expectedResult, result);
    }
}
