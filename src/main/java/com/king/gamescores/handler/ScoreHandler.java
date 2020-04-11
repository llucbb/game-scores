package com.king.gamescores.handler;

import com.king.gamescores.filter.ParameterFilter;
import com.king.gamescores.service.DefaultScoresService;
import com.king.gamescores.service.ScoresService;
import com.king.gamescores.service.SessionKeyService;
import com.king.gamescores.service.SingletonScoresService;
import com.king.gamescores.service.TokenSessionKeyService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.security.SignatureException;
import java.util.Map;
import java.util.logging.Logger;

import static com.king.gamescores.filter.ParameterFilter.SCORE_PARAM;
import static com.king.gamescores.server.HttpMethod.POST;
import static com.king.gamescores.util.HttpMethodValidator.isNotValid;
import static com.king.gamescores.util.ParamsValidator.*;
import static java.net.HttpURLConnection.*;
import static java.util.logging.Level.SEVERE;

public class ScoreHandler implements HttpHandler {

    private static final Logger LOG = Logger.getLogger(ScoreHandler.class.getName());

    private final SessionKeyService sessionKeyService;
    private final ScoresService scoresService;

    public ScoreHandler() {
        sessionKeyService = new TokenSessionKeyService();
        scoresService = SingletonScoresService.getInstance();
    }

    public ScoreHandler(SessionKeyService sessionKeyService) {
        this.sessionKeyService = sessionKeyService;
        scoresService = new DefaultScoresService();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle(HttpExchange exchange) throws IOException {
        if (isNotValid(POST, exchange)) return;

        String path = exchange.getRequestURI().getPath();
        String[] paths = path.split("/");

        String pLevel = paths[1];

        Map<String, Object> params = (Map<String, Object>) exchange.getAttribute(ParameterFilter.PARAMETERS);
        String pScore = (String) params.getOrDefault(SCORE_PARAM, SCORE_PARAM);
        String pSessionKey = (String) params.get(SESSION_KEY);

        if (isNumeric(pLevel) && isSessionKeyProvided(pSessionKey) && isNumeric(pScore)) {

            int userId;
            try {

                userId = sessionKeyService.getUserIdFromSessionKey(pSessionKey);

            } catch (SignatureException e) {
                LOG.log(SEVERE, SESSION_KEY + " is not valid", e);
                ResponseHandler.code(HTTP_UNAUTHORIZED).handle(exchange);
                return;
            }

            try {
                if (sessionKeyService.isSessionKeyValid(pSessionKey)) {
                    LOG.info(SESSION_KEY + " successfully validated");

                    int level = Integer.parseInt(pLevel);
                    int score = Integer.parseInt(pScore);
                    scoresService.registerScore(level, userId, score);

                    LOG.info(String.format("Score %d successfully registered for userid %d at level %d",
                            score, userId, level));
                    ResponseHandler.code(HTTP_OK).handle(exchange);

                } else {
                    LOG.log(SEVERE, SESSION_KEY + " has been expired");
                    ResponseHandler.code(HTTP_UNAUTHORIZED).handle(exchange);
                }

            } catch (SignatureException e) {
                LOG.log(SEVERE, e.getMessage(), e);
                ResponseHandler.code(HTTP_UNAUTHORIZED).handle(exchange);
            }

        } else {
            ResponseHandler.code(HTTP_BAD_REQUEST).handle(exchange);
        }
    }
}
