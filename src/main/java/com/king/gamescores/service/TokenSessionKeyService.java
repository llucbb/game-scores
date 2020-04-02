package com.king.gamescores.service;

import com.king.gamescores.token.TokenBuilder;
import com.king.gamescores.token.TokenParser;

import java.security.SignatureException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Generates a token, a compact and self-contained way to securely generate the required unique session key. Contains a
 * payload and a signature. The payload contains the userId and when expires. Finally, the signature is the hash of the
 * the payload using the hash algorithm, which can be verified and trusted because it is digitally signed.
 */
public class TokenSessionKeyService implements SessionKeyService {

    private static final long EXPIRATION_SECONDS = 600; //10 min

    private final String secretKey;

    private TokenSessionKeyService() {
        secretKey = null;
    }

    /**
     * Constructs a {@link TokenSessionKeyService} with the given secretKey
     *
     * @param secretKey the secret key
     */
    public TokenSessionKeyService(String secretKey) {
        this.secretKey = secretKey;
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
                .setExpiration(LocalDateTime.now().plusSeconds(EXPIRATION_SECONDS))
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
