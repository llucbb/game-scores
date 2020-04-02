package com.king.gamescores.handler;

import com.king.gamescores.service.ScoresService;
import com.king.gamescores.service.SessionKeyService;
import com.king.gamescores.service.SingletonScoresService;
import com.king.gamescores.service.TokenSessionKeyService;
import com.king.gamescores.util.Strings;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.util.logging.Logger;

import static com.king.gamescores.handler.HttpStatus.*;
import static com.king.gamescores.util.ParamsValidator.isNumeric;
import static com.king.gamescores.util.ParamsValidator.isSessionKeyProvided;
import static java.util.logging.Level.SEVERE;

public final class ScoreHandler implements HttpHandler {

    private static final Logger LOG = Logger.getLogger(ScoreHandler.class.getName());

    private final SessionKeyService sessionKeyService;
    private final ScoresService scoresService;

    public ScoreHandler() {
        sessionKeyService = new TokenSessionKeyService("changeit");
        scoresService = SingletonScoresService.getInstance();
    }

    public ScoreHandler(SessionKeyService sessionKeyService) {
        this.sessionKeyService = sessionKeyService;
        scoresService = SingletonScoresService.getInstance();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] paths = path.split("/");

        String levelStr = paths[1];

        String sessionKeyParam = exchange.getRequestURI().getQuery();
        String[] sessionKeyParams = sessionKeyParam != null ? sessionKeyParam.split("=") : null;

        String scoreStr;
        try (InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
            BufferedReader br = new BufferedReader(isr);
            scoreStr = br.readLine();
        }
        if (!Strings.isNotEmpty(scoreStr)) {
            scoreStr = "score";
        }

        if (isNumeric(levelStr) && isSessionKeyProvided(sessionKeyParams) && isNumeric(scoreStr)) {

            String sessionKey = sessionKeyParams[1];
            int userId;
            try {

                userId = sessionKeyService.getUserIdFromSessionKey(sessionKey);

            } catch (SignatureException e) {
                LOG.log(SEVERE, "sessionkey is not valid", e);
                ResponseHandler.status(UNAUTHORIZED).handle(exchange);
                return;
            }

            try {
                if (sessionKeyService.isSessionKeyValid(sessionKey)) {
                    LOG.info("sessionkey successfully validated");

                    int level = Integer.parseInt(levelStr);
                    int score = Integer.parseInt(scoreStr);
                    scoresService.registerScore(level, userId, score);

                    LOG.info(String.format("Score %d successfully registered for userid %d at level %d",
                            score, userId, level));
                    ResponseHandler.status(OK).handle(exchange);

                } else {
                    LOG.log(SEVERE, "sessionkey has been expired");
                    ResponseHandler.status(UNAUTHORIZED).handle(exchange);
                }

            } catch (SignatureException e) {
                LOG.log(SEVERE, e.getMessage(), e);
                ResponseHandler.status(UNAUTHORIZED).handle(exchange);
            }

        } else {
            ResponseHandler.status(BAD_REQUEST).handle(exchange);
        }
    }
}
