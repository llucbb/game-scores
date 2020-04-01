package com.king.gamescores.token;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

/**
 *
 */
public class Signer {

    private static final Logger LOG = Logger.getLogger(Signer.class.getName());

    private static final String ALGORITHM = "HmacSHA512";

    private final Key key;

    public Signer(byte[] keyBytes) {
        this.key = new SecretKeySpec(keyBytes, ALGORITHM);
    }

    public byte[] sign(byte[] data) throws SignatureException {
        Mac mac = getMacInstance();
        return mac.doFinal(data);
    }

    public boolean isValid(String payload, String base64Signature) throws SignatureException {
        byte[] data = payload.getBytes(StandardCharsets.UTF_8);
        byte[] signature = Base64Codec.decode(base64Signature);
        Mac mac = getMacInstance();
        byte[] computed = mac.doFinal(data);
        return MessageDigest.isEqual(computed, signature);
    }

    private Mac getMacInstance() throws SignatureException {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(key);
            return mac;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            LOG.log(SEVERE, e.getMessage(), e);
            throw new SignatureException(e);
        }
    }
}
