package com.king.gamescores.log;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class ScoresLoggerFormatter extends Formatter {

    private static final String FORMAT = "%1$tF %1$tT.%1$tL %2$-7s %3$-50s : %4$s %n";

    @Override
    public synchronized String format(LogRecord lr) {
        return String.format(FORMAT,
                new Date(lr.getMillis()),
                lr.getLevel().getLocalizedName(),
                lr.getLoggerName(),
                lr.getMessage()
        );
    }
}
