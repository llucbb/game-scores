package com.king.gamescores.token;

import com.king.gamescores.util.Strings;
import org.junit.Test;

import java.security.SignatureException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TokenBuilderTest {

    @Test
    public void tokenBuilderShouldGenerateATokenWithValidSignature() throws SignatureException {
        String secretKey = "test";
        String userId = "1";
        long expirationSeconds = 600; //10 min
        LocalDateTime expirationDate = LocalDateTime.now().plusSeconds(expirationSeconds);
        String expiration = DateTimeFormatter.ISO_DATE_TIME.format(expirationDate);
        String payloadExpected = userId + TokenBuilder.SEPARATOR_CHAR + expiration;

        String token = TokenBuilder.builder()
                .signWith(secretKey)
                .setUserId(userId)
                .setExpiration(expirationDate)
                .build();

        assertTrue(Strings.isNotEmpty(token));
        assertEquals(payloadExpected, TokenParser.parser().setSigningKey(secretKey).parse(token));
    }
}
