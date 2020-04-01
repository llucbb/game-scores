package com.king.gamescores.handler;

public enum HttpStatus {

    OK(200), BAD_REQUEST(400), UNAUTHORIZED(401), INTERNAL_SERVER_ERROR(500);

    private final int value;

    HttpStatus(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }
}
