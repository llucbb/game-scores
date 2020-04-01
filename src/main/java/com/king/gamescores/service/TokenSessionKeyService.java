package com.king.gamescores.service;

import com.king.gamescores.token.TokenBuilder;
import com.king.gamescores.token.TokenParser;

import java.security.SignatureException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TokenSessionKeyService implements SessionKeyService {

    private static final long EXPIRATION_SECONDS = 600; //10 min

    private final String secretKey;

    private TokenSessionKeyService() {
        secretKey = null;
    }

    public TokenSessionKeyService(String secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    public String generateSessionKey(int userId) throws SignatureException {
        return TokenBuilder.builder()
                .signWith(secretKey)
                .setUserId(String.valueOf(userId))
                .setExpiration(LocalDateTime.now().plusSeconds(EXPIRATION_SECONDS))
                .build();
    }

    @Override
    public int getUserIdFromSessionKey(String sessionKey) throws SignatureException {
        String payload = TokenParser.parser()
                .setSigningKey(secretKey)
                .parse(sessionKey);
        String[] payloadParts = payload.split("\\" + TokenBuilder.SEPARATOR_CHAR);
        return Integer.parseInt(payloadParts[0]);
    }

    @Override
    public boolean isSessionKeyValid(String sessionKey) throws SignatureException {
        return !isSessionKeyExpired(sessionKey);
    }

    private boolean isSessionKeyExpired(String sessionKey) throws SignatureException {
        String payload = TokenParser.parser()
                .setSigningKey(secretKey)
                .parse(sessionKey);
        String[] payloadParts = payload.split("\\" + TokenBuilder.SEPARATOR_CHAR);
        String expiration = payloadParts[1];
        LocalDateTime expirationDate = LocalDateTime.from(DateTimeFormatter.ISO_DATE_TIME.parse(expiration));
        return expirationDate.isBefore(LocalDateTime.now());
    }
}
