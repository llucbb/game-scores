package com.king.gamescores.handler;

import com.king.gamescores.service.ScoresService;
import com.king.gamescores.service.SingletonScoresService;
import com.king.gamescores.util.Strings;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.logging.Logger;

import static com.king.gamescores.server.HttpMethod.GET;
import static com.king.gamescores.util.HttpMethodValidator.isNotValid;
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
        if (isNotValid(GET, exchange)) return;

        String path = exchange.getRequestURI().getPath();
        String[] paths = path.split("/");

        String pLevel = paths[1];
        if (isNumeric(pLevel)) {

            int level = Integer.parseInt(pLevel);
            String result = scoresService.getHighScoresForLevel(level);

            if (Strings.isNotEmpty(result)) {
                LOG.info(String.format("High score list '%s' has been retrieved for level %d", result, level));
            } else {
                LOG.warning(String.format("High score list empty has been retrieved for level %d", level));
            }
            ResponseHandler.code(HTTP_OK).response(result).handle(exchange);

        } else {
            ResponseHandler.code(HTTP_BAD_REQUEST).handle(exchange);
        }
    }
}
