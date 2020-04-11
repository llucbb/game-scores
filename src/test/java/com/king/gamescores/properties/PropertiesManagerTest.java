package com.king.gamescores.properties;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PropertiesManagerTest {

    private PropertiesManager propertyLoader;

    @Before
    public void setUp() {
        propertyLoader = PropertiesManager.getInstance();
    }

    @Test
    public void getPropertyString() {
        String value = propertyLoader.getProperty("scores.secretKey");

        assertNotNull(value);
        assertEquals("changeit", value);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPropertyStringNotDefinedShouldFail() {
        propertyLoader.getProperty("secores.wromgproperty");
    }

    @Test
    public void getPropertyInt() {
        int value = propertyLoader.getInt("scores.maxScoresPerLevel");

        assertTrue(value > 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPropertyIntNotDefinedShouldFail() {
        propertyLoader.getInt("scores.wromgproperty");
    }

    @Test
    public void getPropertyLong() {
        long value = propertyLoader.getLong("scores.sessionExpiration");

        assertTrue(value > 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPropertyLongNotDefinedShouldFail() {
        propertyLoader.getLong("scores.wromgproperty");
    }
}
