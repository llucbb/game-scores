package com.king.gamescores.handler;

import com.king.gamescores.service.SessionKeyService;
import com.king.gamescores.service.TokenSessionKeyService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.security.SignatureException;
import java.util.logging.Logger;

import static com.king.gamescores.handler.HttpStatus.BAD_REQUEST;
import static com.king.gamescores.handler.HttpStatus.OK;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;

public final class LoginHandler implements HttpHandler {

    private static final Logger LOG = Logger.getLogger(LoginHandler.class.getName());

    private static final String ERR_USER_ID_IS_NOT_NUMERIC = "userid '%s' must be a 31 bit unsigned integer number";

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
        if (isUserIValid(userId)) {

            try {
                String token = sessionKeyService.generateSessionKey(Integer.parseInt(userId));
                LOG.log(INFO, "Token successfully generated");
                ResponseHandler.status(OK).response(token).handle(exchange);

            } catch (SignatureException e) {
                LOG.log(SEVERE, e.getMessage(), e);
                ResponseHandler.status(BAD_REQUEST).response(e.getMessage()).handle(exchange);
            } catch (NumberFormatException e) {
                String msg = String.format(ERR_USER_ID_IS_NOT_NUMERIC, userId);
                LOG.log(SEVERE, msg, e);
                ResponseHandler.status(BAD_REQUEST).response(msg).handle(exchange);
            }
        } else {
            ResponseHandler.status(BAD_REQUEST).handle(exchange);
        }
    }

    private boolean isUserIValid(String userIdParam) {
        // Validate userId path parameter
        try {
            Integer.parseInt(userIdParam);
        } catch (NumberFormatException e) {
            LOG.log(SEVERE, String.format(ERR_USER_ID_IS_NOT_NUMERIC, userIdParam));
            return false;
        }
        return true;
    }

}
