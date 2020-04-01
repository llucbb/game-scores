package com.king.gamescores.service;

public interface SessionKeyService {

    String generateSessionKey(int userId) throws Exception;

    int getUserIdFromSessionKey(String sessionKey) throws Exception;

    boolean isSessionKeyValid(String sessionKey, int userId) throws Exception;
}
