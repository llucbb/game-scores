package com.king.gamescores.util;

import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

public final class ParamsValidator {

    private static final Logger LOG = Logger.getLogger(ParamsValidator.class.getName());

    private static final String ERR_IS_NOT_NUMERIC = "'%s' must be a 31 bit unsigned integer number";
    private static final String SESSION_KEY = "sessionkey";
    private static final String ERR_SESSION_KEY_NOT_PROVIDED = SESSION_KEY + " has not been provided";

    private ParamsValidator() {
    }

    public static boolean isNumeric(String str) {
        try {
            int integer = Integer.parseInt(str);
            if (integer < 1) {
                LOG.log(SEVERE, String.format(ERR_IS_NOT_NUMERIC, str));
                return false;
            }
        } catch (NumberFormatException e) {
            LOG.log(SEVERE, String.format(ERR_IS_NOT_NUMERIC, str));
            return false;
        }
        return true;
    }

    public static boolean isSessionKeyProvided(String[] sessionKeyParams) {
        if (sessionKeyParams == null || sessionKeyParams.length != 2
                || !sessionKeyParams[0].equals(SESSION_KEY) || !Strings.isNotEmpty(sessionKeyParams[1])) {
            LOG.log(SEVERE, ERR_SESSION_KEY_NOT_PROVIDED);
            return false;
        }
        return true;
    }
}
