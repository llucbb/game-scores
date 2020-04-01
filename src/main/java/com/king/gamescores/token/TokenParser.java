package com.king.gamescores.token;

import com.king.gamescores.util.Strings;

import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TokenParser {

    private static final Logger LOG = Logger.getLogger(TokenParser.class.getName());

    private byte[] keyBytes;

    public static TokenParser parser() {
        return new TokenParser();
    }

    public TokenParser setSigningKey(String secretKey) {
        this.keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return this;
    }

    public String parse(String token) throws SignatureException {
        String base64Payload = null;
        String base64Digest = null;
        int delimiterCount = 0;
        StringBuilder sb = new StringBuilder(128);
        char[] arr = token.toCharArray();
        for (char c : arr) {
            if (c == TokenBuilder.SEPARATOR_CHAR) {
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
            throw new IllegalStateException("Token must contain exactly 1 separator characters. Found: " + delimiterCount);
        }

        if (sb.length() > 0) {
            base64Digest = sb.toString();
        }

        if (base64Payload == null) {
            throw new IllegalStateException("Token does not have a payload");
        }

        // Payload
        String payload = new String(Base64Codec.decode(base64Payload), StandardCharsets.UTF_8);

        // Signature
        if (base64Digest != null) {
            if (keyBytes == null || keyBytes.length == 0) {
                throw new IllegalStateException("Secret key is mandatory when having a signature");
            }

            Signer signer = new Signer(keyBytes);
            if (!signer.isValid(payload, base64Digest)) {
                String msg = "Token signature does not match locally computed signature. Token validity cannot be " +
                        "asserted and should not be trusted";
                throw new SignatureException(msg);
            }
        }
        LOG.log(Level.CONFIG, "payload: " + payload);
        return payload;
    }
}
