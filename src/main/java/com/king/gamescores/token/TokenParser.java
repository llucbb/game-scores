package com.king.gamescores.token;

import com.king.gamescores.util.Strings;

import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.util.logging.Logger;

import static com.king.gamescores.token.TokenBuilder.SEPARATOR_CHAR;
import static java.util.logging.Level.FINEST;

/**
 * A token parser for retrieve and validate the token payload
 */
public class TokenParser {

    private static final Logger LOG = Logger.getLogger(TokenParser.class.getName());

    // Secret key byte array
    private byte[] keyBytes;

    /**
     * Returns a new {@link TokenParser} instance that can be configured and then used to parse tokens
     *
     * @return a new {@link TokenParser} instance that can be configured and then used to parse tokens
     */
    public static TokenParser parser() {
        return new TokenParser();
    }

    /**
     * Validates the token payload with the given secretKey
     *
     * @param secretKey signing key used to digitally sign the token.
     * @return the parser for method chaining
     */
    public TokenParser setSigningKey(String secretKey) {
        this.keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return this;
    }

    /**
     * Parses and validates the given token and returns the token payload
     *
     * @param token token to parse and validate
     * @return the token payload parsed
     * @throws SignatureException if there any issue with token payload signature validation
     */
    public String parse(String token) throws SignatureException {
        String base64Payload = null;
        String base64Digest = null;
        int delimiterCount = 0;
        StringBuilder sb = new StringBuilder(128);
        char[] arr = token.toCharArray();
        for (char c : arr) {
            if (c == SEPARATOR_CHAR) {
                CharSequence tokenSeq = Strings.clean(sb);
                String tokenStr = tokenSeq != null ? tokenSeq.toString() : null;
                if (delimiterCount == 0) {
                    base64Payload = tokenStr;
                }
                ++delimiterCount;
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }

        if (delimiterCount != 1) {
            throw new SignatureException("Token must contain exactly 1 separator characters. Found: " + delimiterCount);
        }

        if (sb.length() > 0) {
            base64Digest = sb.toString();
        }

        if (base64Payload == null) {
            throw new SignatureException("Token does not have a payload");
        }

        // Payload
        String payload = new String(Base64Codec.decode(base64Payload), StandardCharsets.UTF_8);

        // Signature
        if (base64Digest != null) {
            if (keyBytes == null || keyBytes.length == 0) {
                throw new IllegalStateException("Secret key is mandatory");
            }

            Signer signer = new Signer(keyBytes);
            if (!signer.isValid(payload, base64Digest)) {
                throw new SignatureException("Token signature does not match locally computed signature. Token " +
                        "validity cannot be asserted and should not be trusted");
            }
        } else {
            throw new SignatureException("Token signature not found");
        }
        LOG.log(FINEST, "payload: " + payload);
        return payload;
    }
}
