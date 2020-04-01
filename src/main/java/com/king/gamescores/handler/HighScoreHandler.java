package com.king.gamescores.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class HighScoreHandler implements HttpHandler {

    private static final Logger LOG = Logger.getLogger(HighScoreHandler.class.getName());

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        LOG.log(Level.CONFIG, "-> handle");

        LOG.log(Level.CONFIG, "<- handle");
    }
}
