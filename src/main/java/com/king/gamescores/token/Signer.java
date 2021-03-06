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
 * Provides a way to check the integrity of information transmitted over or stored in an unreliable medium, based on a
 * secret key. Typically, message authentication codes are used between two parties that share a secret key in order to
 * validate information transmitted between these parties.
 * <p>
 * A {@link Mac} mechanism that is based on cryptographic hash functions is referred to as HMAC. HMAC can be used with
 * any cryptographic hash function, e.g., SHA512, in combination with a secret shared key.
 */
public class Signer {

    private static final Logger LOG = Logger.getLogger(Signer.class.getName());

    private static final String ALGORITHM = "HmacSHA512";

    private final Key key;

    /**
     * Constructs a secret {@link SecretKeySpec} from the given byte array using the name of the secret key algorithm.
     *
     * @param keyBytes the key material of the secret key.
     */
    public Signer(byte[] keyBytes) {
        this.key = new SecretKeySpec(keyBytes, ALGORITHM);
    }

    /**
     * Generates a signature in byte array of the given payload byte array
     *
     * @param payload payload in bytes
     * @return the signature in byte array result.
     * @throws SignatureException if {@link Mac} has not been initialized.
     */
    public byte[] sign(byte[] payload) throws SignatureException {
        Mac mac = getMacInstance();
        return mac.doFinal(payload);
    }

    /**
     * Validates if the given signature in Base64 is valid for the given payload
     *
     * @param payload         the payload of the token to validate
     * @param base64Signature the signature in Base64
     * @return true if the signature is valid for the given payload, false otherwise
     * @throws SignatureException if {@link Mac} has not been initialized.
     */
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
