package com.king.gamescores.token;

import com.king.gamescores.util.Strings;

import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

import static java.util.logging.Level.FINEST;

/**
 * A builder for construct tokens
 */
public class TokenBuilder {

    private static final Logger LOG = Logger.getLogger(TokenBuilder.class.getName());

    // Token payload separator between userId and expiration
    public static final char SEPARATOR_CHAR = '|';

    // Secret key byte array
    private byte[] keyBytes;
    // Token payload string
    private String payload;

    /**
     * Returns a new {@link TokenBuilder} instance that can be configured and then used to create tokens
     *
     * @return a new {@link TokenBuilder} instance that can be configured and then used to create tokens
     */
    public static TokenBuilder builder() {
        return new TokenBuilder();
    }

    /**
     * Signs the token with the given secretKey
     *
     * @param secretKey signing key to use to digitally sign the token.
     * @return the builder for method chaining
     */
    public TokenBuilder signWith(String secretKey) {
        keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return this;
    }

    /**
     * Sets the userId which will form the token payload in order to be retrieved when adding scores per user.
     *
     * @param userId the userId which will form the token payload.
     * @return the builder for method chaining
     */
    public TokenBuilder setUserId(String userId) {
        if (Strings.isNotEmpty(payload)) {
            // expiration has been already set
            payload = userId + SEPARATOR_CHAR + payload;
        } else {
            payload = userId;
        }
        return this;
    }

    /**
     * Sets the expiration string with the ISO-like date-time format, such as '2020-04-02T20:44:00', which will form the
     * token payload in order to check if the token has expired when is used.
     *
     * @param dateTime the token {@link LocalDateTime} payload with the ISO-like date-time format
     * @return the builder for method chaining
     */
    public TokenBuilder setExpiration(LocalDateTime dateTime) {
        String expiration = DateTimeFormatter.ISO_DATE_TIME.format(dateTime);
        if (Strings.isNotEmpty(payload)) {
            // userId has been already set
            payload = payload + SEPARATOR_CHAR + expiration;
        } else {
            payload = expiration;
        }
        return this;
    }

    /**
     * Builds the token
     *
     * @return the token
     * @throws SignatureException if there any issue with token payload signature generation
     */
    public String build() throws SignatureException {
        byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);
        String body = Base64Codec.encode(payloadBytes);

        Signer signer = new Signer(keyBytes);
        byte[] sign = signer.sign(payloadBytes);
        String signature = Base64Codec.encode(sign);

        String token = body + SEPARATOR_CHAR + signature;
        LOG.log(FINEST, "token: " + token);
        return token;
    }
}
