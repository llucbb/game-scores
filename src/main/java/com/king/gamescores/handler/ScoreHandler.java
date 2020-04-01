package com.king.gamescores.handler;

import com.king.gamescores.service.SessionKeyService;
import com.king.gamescores.service.TokenSessionKeyService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.security.SignatureException;
import java.util.logging.Logger;

import static com.king.gamescores.handler.HttpStatus.*;
import static com.king.gamescores.util.ParamsValidator.isNumeric;
import static com.king.gamescores.util.ParamsValidator.isSessionKeyValid;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;

public final class ScoreHandler implements HttpHandler {

    private static final Logger LOG = Logger.getLogger(ScoreHandler.class.getName());

    private final SessionKeyService sessionKeyService;

    public ScoreHandler() {
        sessionKeyService = new TokenSessionKeyService("changeit");
    }

    public ScoreHandler(SessionKeyService sessionKeyService) {
        this.sessionKeyService = sessionKeyService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] paths = path.split("/");

        String level = paths[1];

        String sessionKeyParam = exchange.getRequestURI().getQuery();
        String[] sessionKeyParams = sessionKeyParam != null ? sessionKeyParam.split("=") : null;

        if (isNumeric(level) && isSessionKeyValid(sessionKeyParams)) {

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
                    LOG.log(INFO, String.format("sessionkey successfully validated for userid: %s", userId));
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
