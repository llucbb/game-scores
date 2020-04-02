package com.king.gamescores.server;

import com.king.gamescores.handler.HighScoreHandler;
import com.king.gamescores.handler.LoginHandler;
import com.king.gamescores.handler.ResponseHandler;
import com.king.gamescores.handler.ScoreHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static com.king.gamescores.handler.HttpStatus.BAD_REQUEST;
import static java.util.logging.Level.SEVERE;

public class ScoresServer {

    private static final Logger LOG = Logger.getLogger(ScoresServer.class.getName());

    private static final int DEFAULT_WORKERS = Runtime.getRuntime().availableProcessors() - 1;

    private ScoresServer(int port) throws IOException {
        startServer(port, DEFAULT_WORKERS);
    }

    private ScoresServer(int port, int workers) throws IOException {
        startServer(port, workers);
    }

    public static void start(int port) throws IOException {
        new ScoresServer(port);
    }

    public static void start(int port, int workers) throws IOException {
        new ScoresServer(port, workers);
    }

    private void startServer(int port, int workers) throws IOException {
        boolean started = false;
        ExecutorService executor = Executors.newFixedThreadPool(workers);
        try {
            HttpServer httpServer = HttpServer.create(new InetSocketAddress(port), 0);
            httpServer.createContext("/", this::handle);
            httpServer.setExecutor(executor);
            httpServer.start();
            started = true;
            LOG.info("Server started");
        } catch (IOException e) {
            LOG.log(SEVERE, e.getMessage(), e);
            throw e;
        } finally {
            if (!started) {
                executor.shutdownNow();
                LOG.warning("Server stopped");
            }
        }
    }

    private void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String[] paths = path.split("/");
            if (paths.length == 3) {
                String endpoint = paths[2];
                if (endpoint.equals("login")) {
                    new LoginHandler().handle(exchange);
                    return;
                } else if (endpoint.startsWith("score")) {
                    new ScoreHandler().handle(exchange);
                    return;
                } else if (endpoint.equals("highscorelist")) {
                    new HighScoreHandler().handle(exchange);
                    return;
                }
            }
            ResponseHandler.status(BAD_REQUEST).handle(exchange);

        } catch (IOException e) {
            LOG.log(SEVERE, e.getMessage(), e);
            throw e;
        }
    }
}
