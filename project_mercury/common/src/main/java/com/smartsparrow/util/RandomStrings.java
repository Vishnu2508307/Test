package com.smartsparrow.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Helper to generate random strings.
 *
 */
public class RandomStrings {

    /**
     * Generate a random string of specified length containing only the pool of characters.
     *
     * @param length the length of the random string.
     * @param pool the pool of characters to use, does not de-duplicate the characters in the pool.
     *
     * @return a random string
     */
    public static String random(final int length, final String pool) {
        Preconditions.checkArgument(length > 0, "length must be greater than 0");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(pool), "missing pool characters");

        final char[] buffer = new char[length];
        final int poolSize = pool.length();

        for (int pos = 0; pos < length; pos++) {
            buffer[pos] = pool.charAt(Random.nextInt(poolSize));
        }

        return new String(buffer);
    }
}
