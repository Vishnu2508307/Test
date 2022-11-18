package com.smartsparrow.util;

import java.security.SecureRandom;

/**
 *  Helper class to hold the functions to generate random data
 *
 *  Place holder calls to hold all the random generated helpler functions at one place.
 *
 */

public class Random {

    private static final SecureRandom random = new SecureRandom();


    public static Long  nextLong() {
        return random.nextLong();
    }

    /**
     *  Returns the next pseudorandom, uniformly distributed value if we don't send bound
     *  Inc ase if you send a bound then nextInt will return next Integer in the supplied bound.
     * @param bound
     * @return Random value of type Integer
     */
    public static int nextInt(Integer bound) {
        if(bound == null)
            return random.nextInt();
        else
            return random.nextInt(bound);
    }

}
