package com.king.gamescores.token;

import com.king.gamescores.service.SessionKeyService;
import com.king.gamescores.service.TokenSessionKeyService;
import com.king.gamescores.util.Strings;
import org.junit.Test;

import java.security.SignatureException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;

import static org.junit.Assert.*;

public class TokenBuilderTest {

    private static final String SECRET_KEY = "changeit";
    private static final String USER_ID = "1";

    private final SessionKeyService sessionKeyService = new TokenSessionKeyService();

    @Test
    public void tokenBuilderShouldGenerateATokenWithValidSignature() throws SignatureException {
        long expirationMS = 30000;
        LocalDateTime expirationDate = LocalDateTime.now().plus(expirationMS, ChronoField.MILLI_OF_DAY.getBaseUnit());
        String expiration = DateTimeFormatter.ISO_DATE_TIME.format(expirationDate);
        String payloadExpected = USER_ID + TokenBuilder.SEPARATOR_CHAR + expiration;

        String token = TokenBuilder.builder()
                .signWith(SECRET_KEY)
                .setUserId(USER_ID)
                .setExpiration(expirationDate)
                .build();

        assertTrue(Strings.isNotEmpty(token));
        assertEquals(payloadExpected, TokenParser.parser().setSigningKey(SECRET_KEY).parse(token));
        assertTrue(sessionKeyService.isSessionKeyValid(token));
        assertEquals(Integer.parseInt(USER_ID), sessionKeyService.getUserIdFromSessionKey(token));
    }

    @Test
    public void tokenBuilderExpiredShouldFail() throws SignatureException, InterruptedException {
        long expirationMS = 100;
        LocalDateTime expirationDate = LocalDateTime.now().plus(expirationMS, ChronoField.MILLI_OF_DAY.getBaseUnit());
        String expiration = DateTimeFormatter.ISO_DATE_TIME.format(expirationDate);
        String payloadExpected = USER_ID + TokenBuilder.SEPARATOR_CHAR + expiration;

        String token = TokenBuilder.builder()
                .signWith(SECRET_KEY)
                .setUserId(USER_ID)
                .setExpiration(expirationDate)
                .build();

        Thread.sleep(150);

        assertTrue(Strings.isNotEmpty(token));
        assertEquals(payloadExpected, TokenParser.parser().setSigningKey(SECRET_KEY).parse(token));
        assertFalse(sessionKeyService.isSessionKeyValid(token));
    }
}
