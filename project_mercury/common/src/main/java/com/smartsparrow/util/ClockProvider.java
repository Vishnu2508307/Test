package com.smartsparrow.util;

import java.time.Clock;

import javax.inject.Provider;

/**
 * Provide a consistent UTC clock across the application codebase.
 * <p>
 * Intended to be easily mocked; i.e. to inject a Fixed clock or similar into dependencies.
 */
public class ClockProvider implements Provider<Clock> {

    /**
     * Provide a clock when needed.
     *
     * @return a clock configured to UTC.
     */
    @Override
    public Clock get() {
        return Clock.systemUTC();
    }

}
