package com.king.gamescores.service;

import java.security.SignatureException;

public interface SessionKeyService {

    String generateSessionKey(int userId) throws SignatureException;

    int getUserIdFromSessionKey(String sessionKey) throws SignatureException;

    boolean isSessionKeyValid(String sessionKey) throws SignatureException;
}
