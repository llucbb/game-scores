package com.king.gamescores.handler;

import com.king.gamescores.service.SessionKeyService;
import com.king.gamescores.service.TokenSessionKeyService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.security.SignatureException;
import java.util.logging.Logger;

import static com.king.gamescores.util.ParamsValidator.isNumeric;
import static java.net.HttpURLConnection.*;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;

public final class LoginHandler implements HttpHandler {

    private static final Logger LOG = Logger.getLogger(LoginHandler.class.getName());

    private final SessionKeyService sessionKeyService;

    public LoginHandler() {
        sessionKeyService = new TokenSessionKeyService("changeit");
    }

    public LoginHandler(SessionKeyService sessionKeyService) {
        this.sessionKeyService = sessionKeyService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] paths = path.split("/");

        String userId = paths[1];
        if (isNumeric(userId)) {

            try {
                String token = sessionKeyService.generateSessionKey(Integer.parseInt(userId));
                LOG.log(INFO, "Token successfully generated");
                ResponseHandler.status(HTTP_OK).response(token).handle(exchange);

            } catch (SignatureException e) {
                LOG.log(SEVERE, e.getMessage(), e);
                ResponseHandler.status(HTTP_INTERNAL_ERROR).handle(exchange);
            }

        } else {
            ResponseHandler.status(HTTP_BAD_REQUEST).handle(exchange);
        }
    }
}
