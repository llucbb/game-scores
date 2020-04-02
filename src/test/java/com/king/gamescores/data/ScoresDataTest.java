package com.king.gamescores.data;

import com.king.gamescores.util.Strings;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ScoresDataTest {

    private ScoresData scoresData;

    @Before
    public void setUp() {
        scoresData = ScoresData.getInstance(5);
    }

    @Test
    public void emptyScores() {
        int level = 1;

        String result = scoresData.getHighScoresForLevel(level);

        Assert.assertFalse(Strings.isNotEmpty(result));
    }

    @Test
    public void registerLessThanMaxScores() {
        int level = 1;
        scoresData.registerScore(level, 1, 15);
        scoresData.registerScore(level, 2, 4);
        scoresData.registerScore(level, 2, 43);
        scoresData.registerScore(level, 3, 11);
        scoresData.registerScore(level, 4, 2);
        String expectedResult = "2=43,1=15,3=11,4=2";

        String result = scoresData.getHighScoresForLevel(level);

        Assert.assertEquals(expectedResult, result);
    }

    @Test
    public void registerMoreThanMaxScores() {
        int level = 1;
        scoresData.registerScore(level, 1, 1);
        scoresData.registerScore(level, 1, 3);
        scoresData.registerScore(level, 2, 2);
        scoresData.registerScore(level, 2, 1);
        scoresData.registerScore(level, 3, 15);
        scoresData.registerScore(level, 4, 13);
        scoresData.registerScore(level, 5, 11);
        scoresData.registerScore(level, 6, 12);
        String expectedResult = "3=15,4=13,6=12,5=11,1=3";

        String result = scoresData.getHighScoresForLevel(level);

        Assert.assertEquals(expectedResult, result);
    }
}
