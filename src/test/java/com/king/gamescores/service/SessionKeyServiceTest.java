package com.king.gamescores.service;

import com.king.gamescores.util.Strings;
import org.junit.Test;

import java.security.SignatureException;

import static com.king.gamescores.token.TokenBuilder.SEPARATOR_CHAR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SessionKeyServiceTest {

    private final SessionKeyService sessionKeyService = new TokenSessionKeyService();

    @Test
    public void generateSessionKey() throws SignatureException {
        int userId = 1;

        String sessionKey = sessionKeyService.generateSessionKey(userId);

        assertTrue(Strings.isNotEmpty(sessionKey));
        assertEquals(userId, sessionKeyService.getUserIdFromSessionKey(sessionKey));
        assertTrue(sessionKeyService.isSessionKeyValid(sessionKey));
    }

    @Test(expected = SignatureException.class)
    public void getUserIdFromInvalidSessionKeyShouldFail() throws SignatureException {
        String sessionKey = String.format("fsfsdfsfpj342%skn2nmfsn2", SEPARATOR_CHAR);

        sessionKeyService.getUserIdFromSessionKey(sessionKey);
    }

    @Test(expected = SignatureException.class)
    public void getUserIdFromWithoutPayloadSessionKeyShouldFail() throws SignatureException {
        String sessionKey = String.format("%sfsfsdfsfpj342kn2nmfsn2", SEPARATOR_CHAR);

        sessionKeyService.getUserIdFromSessionKey(sessionKey);
    }

    @Test(expected = SignatureException.class)
    public void getUserIdFromMoreThanOneSeparatorSessionKeyShouldFail() throws SignatureException {
        String sessionKey = String.format("fsf%ssdfsfpj342%skn2nmfsn2", SEPARATOR_CHAR, SEPARATOR_CHAR);

        sessionKeyService.getUserIdFromSessionKey(sessionKey);
    }
}
