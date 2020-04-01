package com.king.gamescores.log;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public final class ScoresLogger {

    public static void setup() throws IOException {
        Logger root = LogManager.getLogManager().getLogger("");
        root.setLevel(Level.ALL);
        for (Handler handler : root.getHandlers()) {
            root.removeHandler(handler);
        }

        Logger.getLogger("com.sun.net.httpserver").setLevel(Level.OFF);

        ScoresFormatter logFormatter = new ScoresFormatter();

        // Create a logger console handler
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.ALL);
        ch.setFormatter(logFormatter);
        root.addHandler(ch);

        // Create a INFO logger file handler
        FileHandler fh = new FileHandler("game-scores.log", false);
        fh.setFormatter(logFormatter);
        fh.setLevel(Level.INFO);
        root.addHandler(fh);

        // Create a DEBUG (aka FINE) logger file handler
        fh = new FileHandler("game-scores-debug.log", true);
        fh.setFormatter(logFormatter);
        fh.setLevel(Level.FINEST);
        root.addHandler(fh);

    }
}
