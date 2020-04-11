package com.king.gamescores.util;

import com.king.gamescores.handler.ResponseHandler;
import com.king.gamescores.server.HttpMethod;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

import static java.net.HttpURLConnection.HTTP_BAD_METHOD;

public class HttpMethodValidator {

    public static boolean isNotValid(HttpMethod methodExpected, HttpExchange exchange) throws IOException {
        try {
            HttpMethod method = HttpMethod.valueOf(exchange.getRequestMethod());
            if (!methodExpected.equals(method)) {
                ResponseHandler.code(HTTP_BAD_METHOD).handle(exchange);
                return true;
            }
        } catch (IllegalArgumentException e) {
            ResponseHandler.code(HTTP_BAD_METHOD).handle(exchange);
            return true;
        }
        return false;
    }
}
