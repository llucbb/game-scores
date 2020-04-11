package com.king.gamescores.server;

public enum HttpMethod {

    GET, POST;

    @Override
    public String toString() {
        return this.name();
    }
}
