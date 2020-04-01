package com.king.gamescores.handler;

import com.king.gamescores.util.Strings;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public final class ResponseHandler implements HttpHandler {

    private final HttpStatus httpStatus;

    private String body;

    private ResponseHandler(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public static ResponseHandler status(HttpStatus httpStatus) {
        return new ResponseHandler(httpStatus);
    }

    public ResponseHandler response(String body) {
        this.body = body;
        return this;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", "text/plain; charset=utf-8");
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        exchange.sendResponseHeaders(httpStatus.value(), bao.size());
        try (OutputStream out = exchange.getResponseBody()) {
            if (Strings.isNotEmpty(body)) {
                out.write(body.getBytes(StandardCharsets.UTF_8));
            } else {
                bao.writeTo(out);
            }
        }
    }
}
