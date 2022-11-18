package com.smartsparrow.util;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.UUID;

import com.google.common.io.BaseEncoding;

/**
 * Supplies convenience method to generate a token
 */
public class Tokens {

    private static class Holder {
        static final SecureRandom randomGenerator = new SecureRandom();
    }

    /**
     * Generates a random token
     * Uses a random uuid (16-bits) + a random long (to pad out the base64)
     *
     * @return a {@link String} representation of the token
     */
    public static String generate() {
        // use a random uuid (16-bits) + a random long (to pad out the base64).
        UUID uuid = UUID.randomUUID();
        long randLong = Holder.randomGenerator.nextLong();

        ByteBuffer bb = ByteBuffer.wrap(new byte[24]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        bb.putLong(randLong);
        byte[] array = bb.array();

        return BaseEncoding.base64Url().encode(array);
    }

    /**
     * Generate a token string, base64 url encoded.
     *
     * @param numBytes the number of bytes to generate, must be > 0
     *
     * @return a {@link String} representation of the token
     */
    public static String generate(final int numBytes) {
        affirmArgument(numBytes > 0, "num bytes must be positive");

        byte[] array = new byte[numBytes];
        Holder.randomGenerator.nextBytes(array);
        return BaseEncoding.base64Url().encode(array);
    }

}
