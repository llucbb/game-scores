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

    @Test
    public void registerMoreThanMaxScores2() {
        int level = 1;
        scoresService.registerScore(level, 1, 1);
        scoresService.registerScore(level, 1, 3);
        scoresService.registerScore(level, 2, 2);
        scoresService.registerScore(level, 2, 1);
        scoresService.registerScore(level, 3, 15);
        scoresService.registerScore(level, 4, 13);
        scoresService.registerScore(level, 5, 11);
        scoresService.registerScore(level, 6, 12);
        scoresService.registerScore(level, 6, 12);
        scoresService.registerScore(level, 1, 1);
        scoresService.registerScore(level, 7, 12);
        String expectedResult = "3=15,4=13,6=12,7=12,5=11";

        String result = scoresService.getHighScoresForLevel(level);

        Assert.assertEquals(expectedResult, result);
    }

    @Test
    public void registerMoreThanMaxScores3() {
        int level = 1;
        scoresService.registerScore(level, 7, 12);
        scoresService.registerScore(level, 1, 2);
        scoresService.registerScore(level, 2, 2);
        scoresService.registerScore(level, 4, 4);
        scoresService.registerScore(level, 3, 24);
        scoresService.registerScore(level, 5, 33);
        scoresService.registerScore(level, 5, 12);
        scoresService.registerScore(level, 6, 2);
        scoresService.registerScore(level, 7, 12);
        scoresService.registerScore(level, 2, 1);
        scoresService.registerScore(level, 7, 24);
        String expectedResult = "5=33,3=24,7=24,4=4,2=2";

        String result = scoresService.getHighScoresForLevel(level);

        Assert.assertEquals(expectedResult, result);
    }

    @Test
    public void registerMoreThanMaxScores4() {
        int level = 1;
        scoresService.registerScore(level, 1, 1);
        scoresService.registerScore(level, 2, 2);
        scoresService.registerScore(level, 3, 3);
        scoresService.registerScore(level, 4, 4);
        scoresService.registerScore(level, 5, 5);
        scoresService.registerScore(level, 6, 1);
        scoresService.registerScore(level, 5, 6);
        scoresService.registerScore(level, 6, 7);
        scoresService.registerScore(level, 7, 8);
        scoresService.registerScore(level, 7, 7);
        scoresService.registerScore(level, 8, 3);
        String expectedResult = "7=8,6=7,5=6,4=4,3=3";

        String result = scoresService.getHighScoresForLevel(level);

        Assert.assertEquals(expectedResult, result);
    }

    @Test
    public void registerMoreThanMaxScores5() {
        int level = 1;
        scoresService.registerScore(level, 1, 10);
        scoresService.registerScore(level, 1, 10);
        scoresService.registerScore(level, 2, 9);
        scoresService.registerScore(level, 3, 8);
        scoresService.registerScore(level, 4, 11);
        scoresService.registerScore(level, 5, 7);
        scoresService.registerScore(level, 5, 8);
        scoresService.registerScore(level, 6, 1);
        scoresService.registerScore(level, 7, 12);
        scoresService.registerScore(level, 7, 12);
        scoresService.registerScore(level, 7, 1);
        String expectedResult = "7=12,4=11,1=10,2=9,5=8";

        String result = scoresService.getHighScoresForLevel(level);

        Assert.assertEquals(expectedResult, result);
    }

    @Test
    public void registerMoreThanMaxScores6() {
        int level = 1;
        scoresService.registerScore(level, 6, 5);
        scoresService.registerScore(level, 5, 6);
        scoresService.registerScore(level, 4, 7);
        scoresService.registerScore(level, 3, 8);
        scoresService.registerScore(level, 2, 9);
        scoresService.registerScore(level, 1, 10);
        scoresService.registerScore(level, 1, 11);
        scoresService.registerScore(level, 2, 10);
        scoresService.registerScore(level, 3, 9);
        scoresService.registerScore(level, 4, 8);
        scoresService.registerScore(level, 5, 7);
        scoresService.registerScore(level, 6, 6);
        String expectedResult = "1=11,2=10,3=9,4=8,5=7";

        String result = scoresService.getHighScoresForLevel(level);

        Assert.assertEquals(expectedResult, result);
    }

}
