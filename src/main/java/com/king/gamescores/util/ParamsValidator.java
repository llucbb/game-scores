package com.king.gamescores.util;

import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

/**
 * Utility class for endpoints request parameters validation
 */
public final class ParamsValidator {

    private static final Logger LOG = Logger.getLogger(ParamsValidator.class.getName());

    public static final String SESSION_KEY = "sessionkey";

    private static final String ERR_IS_NOT_NUMERIC = "'%s' must be a 31 bit unsigned integer number";
    private static final String ERR_SESSION_KEY_NOT_PROVIDED = SESSION_KEY + " has not been provided";

    private ParamsValidator() {
    }

    /**
     * Validates if the given string is a valid numeric value, a 31 bit unsigned integer number
     *
     * @param string to validate if is a valid 31 bit unsigned integer number
     * @return true if the given string is a valid numeric value
     */
    public static boolean isNumeric(String string) {
        try {
            int integer = Integer.parseInt(string);
            if (integer < 1) {
                LOG.log(SEVERE, String.format(ERR_IS_NOT_NUMERIC, string));
                return false;
            }
        } catch (NumberFormatException e) {
            LOG.log(SEVERE, String.format(ERR_IS_NOT_NUMERIC, string));
            return false;
        }
        return true;
    }

    /**
     * Validates if the session key value and param name have been provided
     *
     * @param sessionKey the value of the session key
     * @return true if the session key value and param name have been provided, false otherwise
     */
    public static boolean isSessionKeyProvided(String sessionKey) {
        if (!Strings.isNotEmpty(sessionKey)) {
            LOG.log(SEVERE, ERR_SESSION_KEY_NOT_PROVIDED);
            return false;
        }
        return true;
    }
}
