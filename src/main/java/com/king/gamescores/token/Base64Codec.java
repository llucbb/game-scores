package com.king.gamescores.token;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;

public class Base64Codec {

    public static String encode(byte[] data) {
        String base64Text = DatatypeConverter.printBase64Binary(data);
        byte[] bytes = base64Text.getBytes(StandardCharsets.UTF_8);
        bytes = removePadding(bytes);

        for (int i = 0; i < bytes.length; ++i) {
            if (bytes[i] == 43) {
                bytes[i] = 45;
            } else if (bytes[i] == 47) {
                bytes[i] = 95;
            }
        }

        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static byte[] removePadding(byte[] bytes) {
        byte[] result = bytes;

        int paddingCount = 0;
        for (int i = bytes.length - 1; i > 0; i--) {
            if (bytes[i] == '=') {
                paddingCount++;
            } else {
                break;
            }
        }
        if (paddingCount > 0) {
            result = new byte[bytes.length - paddingCount];
            System.arraycopy(bytes, 0, result, 0, bytes.length - paddingCount);
        }

        return result;
    }

    public static byte[] decode(String encoded) {
        char[] chars = encoded.toCharArray();
        chars = ensurePadding(chars);
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '-') {
                chars[i] = '+';
            } else if (chars[i] == '_') {
                chars[i] = '/';
            }
        }
        return DatatypeConverter.parseBase64Binary(new String(chars));
    }

    private static char[] ensurePadding(char[] chars) {
        char[] result = chars;

        int paddingCount = 0;
        int remainder = chars.length % 4;
        if (remainder == 2 || remainder == 3) {
            paddingCount = 4 - remainder;
        }

        if (paddingCount > 0) {
            result = new char[chars.length + paddingCount];
            System.arraycopy(chars, 0, result, 0, chars.length);
            for (int i = 0; i < paddingCount; i++) {
                result[chars.length + i] = '=';
            }
        }

        return result;
    }
}
