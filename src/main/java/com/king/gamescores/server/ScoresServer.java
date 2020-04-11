package com.king.gamescores.server;

import com.king.gamescores.filter.ParameterFilter;
import com.king.gamescores.handler.HighScoreHandler;
import com.king.gamescores.handler.LoginHandler;
import com.king.gamescores.handler.ResponseHandler;
import com.king.gamescores.handler.ScoreHandler;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.util.logging.Level.CONFIG;
import static java.util.logging.Level.SEVERE;

public class ScoresServer {

    private static final Logger LOG = Logger.getLogger(ScoresServer.class.getName());

    private static final int DEFAULT_WORKERS = Runtime.getRuntime().availableProcessors() - 1;

    private HttpServer httpServer;
    private ExecutorService executor;

    private LoginHandler loginHandler;
    private ScoreHandler scoreHandler;
    private HighScoreHandler highScoreHandler;

    private ScoresServer(int port) throws IOException {
        this.loginHandler = new LoginHandler();
        this.scoreHandler = new ScoreHandler();
        this.highScoreHandler = new HighScoreHandler();
        startServer(port, DEFAULT_WORKERS);
    }

    private ScoresServer(int port, int workers) throws IOException {
        this.loginHandler = null;
        this.scoreHandler = null;
        this.highScoreHandler = null;
        startServer(port, workers);
    }

    public static ScoresServer start(int port) throws IOException {
        return new ScoresServer(port);
    }

    public static ScoresServer start(int port, int workers) throws IOException {
        return new ScoresServer(port, workers);
    }

    public ScoresServer loginHandler(LoginHandler loginHandler) {
        this.loginHandler = loginHandler;
        return this;
    }

    public ScoresServer scoreHandler(ScoreHandler scoreHandler) {
        this.scoreHandler = scoreHandler;
        return this;
    }

    public ScoresServer highScoreHandler(HighScoreHandler highScoreHandler) {
        this.highScoreHandler = highScoreHandler;
        return this;
    }

    private void startServer(int port, int workers) throws IOException {
        boolean started = false;
        executor = Executors.newFixedThreadPool(workers);
        try {
            httpServer = HttpServer.create(new InetSocketAddress(port), 0);
            HttpContext context = httpServer.createContext("/", this::handle);
            context.getFilters().add(new ParameterFilter());
            httpServer.setExecutor(executor);
            httpServer.start();
            started = true;
            LOG.log(CONFIG, "Server started");
        } catch (IOException e) {
            LOG.log(SEVERE, e.getMessage(), e);
            throw e;
        } finally {
            if (!started) {
                executor.shutdownNow();
            }
        }
    }

    public void stopServer(int delay) {
        executor.shutdownNow();
        httpServer.stop(delay);
        LOG.log(CONFIG, "Server stopped");
    }

    private void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] paths = path.split("/");
        if (paths.length == 3) {
            String endpoint = paths[2];
            if (endpoint.equals("login")) {
                loginHandler.handle(exchange);
                return;
            } else if (endpoint.startsWith("score")) {
                scoreHandler.handle(exchange);
                return;
            } else if (endpoint.equals("highscorelist")) {
                highScoreHandler.handle(exchange);
                return;
            }
        }
        ResponseHandler.code(HTTP_BAD_REQUEST).handle(exchange);
    }
}
