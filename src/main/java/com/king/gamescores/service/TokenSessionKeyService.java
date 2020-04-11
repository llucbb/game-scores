package com.king.gamescores.service;

import com.king.gamescores.properties.PropertiesManager;
import com.king.gamescores.token.TokenBuilder;
import com.king.gamescores.token.TokenParser;

import java.security.SignatureException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;

/**
 * Generates a token, a compact and self-contained way to securely generate the required unique session key. Contains a
 * payload and a signature. The payload contains the userId and when expires. Finally, the signature is the hash of the
 * the payload using the hash algorithm, which can be verified and trusted because it is digitally signed.
 */
public class TokenSessionKeyService implements SessionKeyService {

    private static final String SECRET_KEY = "scores.secretKey";
    private static final String SESSION_EXPIRATION = "scores.sessionExpiration";

    private final String secretKey;
    private final long expirationMS;

    /**
     * Constructs a {@link TokenSessionKeyService}
     */
    public TokenSessionKeyService() {
        PropertiesManager propertiesManager = PropertiesManager.getInstance();
        secretKey = propertiesManager.getString(SECRET_KEY);
        expirationMS = propertiesManager.getLong(SESSION_EXPIRATION);
    }

    /**
     * Generate the unique session key, a token in the form of a string.
     *
     * @param userId The user identifier
     * @return The session key in string format
     * @throws SignatureException
     */
    @Override
    public String generateSessionKey(int userId) throws SignatureException {
        return TokenBuilder.builder()
                .signWith(secretKey)
                .setUserId(String.valueOf(userId))
                .setExpiration(LocalDateTime.now().plus(expirationMS, ChronoField.MILLI_OF_DAY.getBaseUnit()))
                .build();
    }

    /**
     * Retrieve userId for the given session key. Will be referenced with the score provided when adding scores per
     * level.
     *
     * @param sessionKey The session key in string format
     * @return
     * @throws SignatureException
     */
    @Override
    public int getUserIdFromSessionKey(String sessionKey) throws SignatureException {
        String payload = TokenParser.parser()
                .setSigningKey(secretKey)
                .parse(sessionKey);
        String[] payloadParts = payload.split("\\" + TokenBuilder.SEPARATOR_CHAR);
        return Integer.parseInt(payloadParts[0]);
    }

    /**
     * Checks the expiration of the given session key.
     *
     * @param sessionKey The session key in string format
     * @return true if the session key is valid, false otherwise
     * @throws SignatureException
     */
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
