package com.king.gamescores.token;

import com.king.gamescores.util.Strings;

import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TokenBuilder {

    private static final Logger LOG = Logger.getLogger(TokenBuilder.class.getName());

    public static final char SEPARATOR_CHAR = '|';

    private byte[] keyBytes;
    private String payload;

    public static TokenBuilder builder() {
        return new TokenBuilder();
    }

    public TokenBuilder signWith(String secretKey) {
        keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return this;
    }

    public TokenBuilder setUserId(String userId) {
        if (Strings.isNotEmpty(payload)) {
            // expiration has been already set
            payload = userId + SEPARATOR_CHAR + payload;
        } else {
            payload = userId;
        }
        return this;
    }

    public TokenBuilder setExpiration(LocalDateTime exp) {
        String expiration = DateTimeFormatter.ISO_DATE_TIME.format(exp);
        if (Strings.isNotEmpty(payload)) {
            // userId has been already set
            payload = payload + SEPARATOR_CHAR + expiration;
        } else {
            payload = expiration;
        }
        return this;
    }

    public String build() throws SignatureException {
        byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);
        String body = Base64Codec.encode(payloadBytes);

        Signer signer = new Signer(keyBytes);
        byte[] sign = signer.sign(payloadBytes);
        String signature = Base64Codec.encode(sign);

        String token = body + SEPARATOR_CHAR + signature;
        LOG.log(Level.CONFIG, "token: " + token);
        return token;
    }
}
