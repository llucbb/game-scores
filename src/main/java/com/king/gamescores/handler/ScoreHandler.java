package com.king.gamescores.handler;

import com.king.gamescores.service.SessionKeyService;
import com.king.gamescores.service.TokenSessionKeyService;
import com.king.gamescores.util.Strings;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.security.SignatureException;
import java.util.logging.Logger;

import static com.king.gamescores.handler.HttpStatus.BAD_REQUEST;
import static com.king.gamescores.handler.HttpStatus.OK;
import static java.util.logging.Level.*;

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
        LOG.log(CONFIG, "-> handle");

        String path = exchange.getRequestURI().getPath();
        String[] paths = path.split("/");

        String level = paths[1];
        if (!Strings.isNumeric(level)) {
            String msg = String.format(ERR_LEVEL_IS_NOT_NUMERIC, level);
            LOG.log(SEVERE, msg);
            ResponseHandler.status(BAD_REQUEST).response(msg).handle(exchange);
            return;
        }

        // Validate sessionkey
        String query = exchange.getRequestURI().getQuery();
        if (!Strings.isNotEmpty(query)) {
            LOG.log(SEVERE, ERR_SESSION_KEY_NOT_PROVIDED);
            ResponseHandler.status(BAD_REQUEST).response(ERR_SESSION_KEY_NOT_PROVIDED).handle(exchange);
            return;
        }
        String[] queries = query.split("=");
        if (queries.length != 2 && !queries[0].equals("sessionkey") && !Strings.isNotEmpty(queries[1])) {
            LOG.log(SEVERE, ERR_SESSION_KEY_NOT_PROVIDED);
            ResponseHandler.status(BAD_REQUEST).response(ERR_SESSION_KEY_NOT_PROVIDED).handle(exchange);
            return;
        }

        String sessionKey = queries[1];
        try {

            int userId = sessionKeyService.getUserIdFromSessionKey(sessionKey);
            if (sessionKeyService.isSessionKeyValid(sessionKey)) {
                LOG.log(INFO, String.format("userId successfully generated : %s", userId));
                ResponseHandler.status(OK).response(String.valueOf(userId)).handle(exchange);
            } else {
                LOG.log(SEVERE, String.format("userId error generated : %s", userId));
                ResponseHandler.status(BAD_REQUEST).response(String.valueOf(userId)).handle(exchange);
            }

        } catch (SignatureException e) {
            LOG.log(SEVERE, e.getMessage(), e);
            ResponseHandler.status(BAD_REQUEST).response(e.getMessage()).handle(exchange);
        }


        LOG.log(CONFIG, "<- handle");
    }
}
