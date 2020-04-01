package com.king.gamescores.server;

import com.king.gamescores.handler.HighScoreHandler;
import com.king.gamescores.handler.LoginHandler;
import com.king.gamescores.handler.ScoreHandler;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScoresServer {

    private static final int DEFAULT_WORKERS = Runtime.getRuntime().availableProcessors() - 1;

    public static void start(int port) throws IOException {
        new ScoresServer(port);
    }

    public static void start(int port, int workers) throws IOException {
        new ScoresServer(port, workers);
    }

    private ScoresServer(int port) throws IOException {
        startServer(port, DEFAULT_WORKERS);
    }

    private ScoresServer(int port, int workers) throws IOException {
        startServer(port, workers);
    }

    private void startServer(int port, int workers) throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        httpServer.createContext("/", this::handle);
        boolean started = false;
        ExecutorService executor = Executors.newFixedThreadPool(workers);
        try {
            httpServer.setExecutor(executor);
            httpServer.start();
            started = true;
            System.out.println("Server started");
        } finally {
            if (!started) {
                executor.shutdownNow();
            }
        }
    }

    private void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");
            if (parts.length == 3) {
                String endpoint = parts[2];
                if (endpoint.equals("login")) {
                    new LoginHandler().handle(exchange);
                } else if (endpoint.startsWith("score")) {
                    new ScoreHandler().handle(exchange);
                } else if (endpoint.equals("highscorelist")) {
                    new HighScoreHandler().handle(exchange);
                }
            }
            badRequest(exchange);

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            throw ex;
        }
    }

    private void badRequest(HttpExchange exchange) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", "text/plain; charset=utf-8");
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        exchange.sendResponseHeaders(400, bao.size());
        try (OutputStream out = exchange.getResponseBody()) {
            bao.writeTo(out);

        }
    }
}
