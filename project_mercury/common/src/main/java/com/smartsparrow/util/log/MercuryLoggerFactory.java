package com.smartsparrow.util.log;

import org.slf4j.LoggerFactory;

public class MercuryLoggerFactory {

    public static <T> MercuryLogger getLogger(final Class<T> clazz) {
        return new MercuryLoggerImpl(LoggerFactory.getLogger(clazz));
    }
}
