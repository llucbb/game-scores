package com.king.gamescores.handler;

import com.king.gamescores.service.ScoresService;
import com.king.gamescores.service.SingletonScoresService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.logging.Logger;

import static com.king.gamescores.util.ParamsValidator.isNumeric;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;

public class HighScoreHandler implements HttpHandler {

    private static final Logger LOG = Logger.getLogger(HighScoreHandler.class.getName());

    private final ScoresService scoresService;

    public HighScoreHandler() {
        scoresService = SingletonScoresService.getInstance();
    }

    public HighScoreHandler(ScoresService scoresService) {
        this.scoresService = scoresService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] paths = path.split("/");

        String levelStr = paths[1];

        if (isNumeric(levelStr)) {

            int level = Integer.parseInt(levelStr);
            String result = scoresService.getHighScoresForLevel(level);

            LOG.info(String.format("High score list %s has been successfully retrieved", result));
            ResponseHandler.code(HTTP_OK).response(result).handle(exchange);

        } else {
            ResponseHandler.code(HTTP_BAD_REQUEST).handle(exchange);
        }
    }
}
