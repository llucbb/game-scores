package com.king.gamescores.properties;

import com.king.gamescores.util.Strings;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystemNotFoundException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropertiesManager {

    private static final Logger LOG = Logger.getLogger(PropertiesManager.class.getName());

    private static final String PROPERTY_IS_MANDATORY = " property is mandatory";
    private static final String APPLICATION_PROPERTIES = "/scores.properties";

    private static PropertiesManager instance = null;
    private final Properties properties;

    private PropertiesManager() {
        properties = new Properties();
        try {
            try (InputStream in = PropertiesManager.class.getResourceAsStream(APPLICATION_PROPERTIES)) {
                properties.load(in);
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            throw new FileSystemNotFoundException(e.getMessage());
        }
    }

    public static PropertiesManager getInstance() {
        if (instance == null) {
            synchronized (PropertiesManager.class) {
                if (instance == null) {
                    instance = new PropertiesManager();
                }
            }
        }
        return instance;
    }

    public String getString(String property) {
        String value = properties.getProperty(property);
        if (!Strings.isNotEmpty(value)) {
            throw new IllegalArgumentException(property + PROPERTY_IS_MANDATORY);
        }
        return value;
    }

    public int getInt(String property) {
        int value = Integer.parseInt((String) properties.get(property));
        if (value <= 0) {
            throw new IllegalArgumentException(property + PROPERTY_IS_MANDATORY);
        }
        return value;
    }

    public long getLong(String property) {
        long value = Long.parseLong((String) properties.get(property));
        if (value <= 0) {
            throw new IllegalArgumentException(property + PROPERTY_IS_MANDATORY);
        }
        return value;
    }
}
