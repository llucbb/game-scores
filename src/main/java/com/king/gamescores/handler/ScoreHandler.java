package com.king.gamescores.handler;

import com.king.gamescores.service.SessionKeyService;
import com.king.gamescores.service.TokenSessionKeyService;
import com.king.gamescores.util.Strings;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.security.SignatureException;
import java.util.logging.Logger;

import static com.king.gamescores.handler.HttpStatus.*;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;

public final class ScoreHandler implements HttpHandler {

    private static final Logger LOG = Logger.getLogger(ScoreHandler.class.getName());

    private static final String ERR_LEVEL_IS_NOT_NUMERIC = "levelid '%s' must be a 31 bit unsigned integer number";
    private static final String ERR_SESSION_KEY_NOT_PROVIDED = "sessionkey has not been provided";


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

        if (isLevelValid(level) && isSessionKeyValid(sessionKeyParams)) {

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

    private boolean isLevelValid(String levelParam) {
        // Validate level path parameter
        String msg = String.format(ERR_LEVEL_IS_NOT_NUMERIC, levelParam);
        try {
            Integer.parseInt(levelParam);
        } catch (NumberFormatException e) {
            LOG.log(SEVERE, msg);
            return false;
        }
        return true;
    }

    private boolean isSessionKeyValid(String[] sessionKeyParams) {
        // Validate sessionkey query parameter
        if (sessionKeyParams == null || sessionKeyParams.length != 2
                || !sessionKeyParams[0].equals("sessionkey") || !Strings.isNotEmpty(sessionKeyParams[1])) {
            LOG.log(SEVERE, ERR_SESSION_KEY_NOT_PROVIDED);
            return false;
        }
        return true;
    }
}
