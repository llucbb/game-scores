package com.king.gamescores;

import com.king.gamescores.log.ScoresLogger;
import com.king.gamescores.server.ScoresServer;

import java.io.IOException;

public class Main {

    private static final int PORT = 8081;

    public static void main(String[] args) throws IOException {
        ScoresLogger.setup();

        ScoresServer.start(PORT);
    }
}
