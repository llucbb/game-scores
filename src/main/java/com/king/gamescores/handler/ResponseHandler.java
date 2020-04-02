package com.king.gamescores.handler;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ResponseHandler implements HttpHandler {

    private final int responseCode;

    private String body = "";

    private ResponseHandler(int responseCode) {
        this.responseCode = responseCode;
    }

    public static ResponseHandler code(int responseCode) {
        return new ResponseHandler(responseCode);
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
        exchange.sendResponseHeaders(responseCode, body.length());
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body.getBytes(StandardCharsets.UTF_8));
        }
    }
}
