package com.king.gamescores.token;

import org.junit.Assert;
import org.junit.Test;

import java.security.SignatureException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TokenBuilderTest {

    @Test
    public void tokenBuilderShouldGenerateATokenWithValidSignature() throws SignatureException {
        String secretKey = "changeit";
        String userId = "userId";
        long expirationSeconds = 600; //10 min
        LocalDateTime expirationDate = LocalDateTime.now().plusSeconds(expirationSeconds);
        String expiration = DateTimeFormatter.ISO_DATE_TIME.format(expirationDate);
        String payloadExpected = userId + TokenBuilder.SEPARATOR_CHAR + expiration;

        String token = TokenBuilder.builder()
                .signWith(secretKey)
                .setUserId(userId)
                .setExpiration(expirationDate)
                .build();

        Assert.assertNotNull(token);
        Assert.assertFalse(token.isEmpty());
        Assert.assertEquals(payloadExpected, TokenParser.parser().setSigningKey(secretKey).parse(token));
    }
}
