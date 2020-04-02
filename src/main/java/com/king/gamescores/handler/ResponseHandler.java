package com.king.gamescores.handler;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public final class ResponseHandler implements HttpHandler {

    private final HttpStatus httpStatus;

    private String body = "";

    private ResponseHandler(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public static ResponseHandler status(HttpStatus httpStatus) {
        return new ResponseHandler(httpStatus);
    }

    public ResponseHandler response(String body) {
        if (body != null) {
            this.body = body;
        }
        return this;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(httpStatus.value(), body.length());
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body.getBytes(StandardCharsets.UTF_8));
        }
    }
}
