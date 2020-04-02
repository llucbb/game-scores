package com.king.gamescores.util;

/**
 * Utility class for Strings
 */
public final class Strings {

    private Strings() {
    }

    public static CharSequence clean(CharSequence str) {
        str = trimWhitespace(str);
        if (!isNotEmpty(str)) {
            return null;
        }
        return str;
    }

    private static CharSequence trimWhitespace(CharSequence str) {
        if (!isNotEmpty(str)) {
            return str;
        }
        final int length = str.length();

        int start = 0;
        while (start < length && Character.isWhitespace(str.charAt(start))) {
            start++;
        }

        int end = length;
        while (start < length && Character.isWhitespace(str.charAt(end - 1))) {
            end--;
        }
        return ((start > 0) || (end < length)) ? str.subSequence(start, end) : str;
    }

    public static boolean isNotEmpty(CharSequence str) {
        return str != null && str.length() > 0;
    }
}
