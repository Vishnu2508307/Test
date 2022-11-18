package com.smartsparrow.util;

import java.util.UUID;

import com.smartsparrow.exception.IllegalArgumentFault;

/**
 * Supply static utility methods for working with UUIDs.
 */
public class UUIDs {


    /**
     * Creates a new time-based (version 1) UUID.
     *
     * @return a new time-based UUID.
     */
    public static UUID timeBased() {
        return com.datastax.driver.core.utils.UUIDs.timeBased();
    }

    /**
     * Create a time-based (version 1) UUID, encoded with the supplied timestamp
     *
     * Note: usage of this method should be limited
     *
     * @param epochMillis the epoch millis to be encoded into the UUID.
     * @return a new time-based UUID.
     */
    public static UUID timeBased(final long epochMillis) {
        // This is based on the suggestion in the DataStax UUIDs timeBased() javadoc;
        long mostSigBits = com.datastax.driver.core.utils.UUIDs.startOf(epochMillis).getMostSignificantBits();
        long r = Random.nextLong();
        return new UUID(mostSigBits, r);
    }

    /**
     * Extract the millis since the epoch from the supplied UUID.
     *
     * @param uuid the UUID to decode
     * @return the epoch millis encoded within the UUID
     * @throws IllegalArgumentException if the supplied UUID is not a version 1 UUID
     */
    public static long epochMillisOf(final UUID uuid) {
        if (uuid.version() != 1) {
            throw new IllegalArgumentException(
                    String.format("Can only retrieve the unix timestamp for version 1 uuid (provided version %d)",
                                  uuid.version()));
        }

        return com.datastax.driver.core.utils.UUIDs.unixTimestamp(uuid);
    }

    /**
     * Creates a new random (version 4) UUID.
     *
     * <p>This method is just a convenience for {@code UUID.randomUUID()}.
     *
     * @return a new random UUID
     */
    public static UUID random() {
        return UUID.randomUUID();
    }

    /**
     * Creates a {@code UUID} from the string standard representation as
     * described in the {@link #toString} method.
     *
     * This method is just a convenience for {@code UUID.fromString()}.
     *
     * @param value A string that specifies a {@code UUID}
     * @return A {@code UUID} with the specified value
     * @throws IllegalArgumentFault If name does not conform to the string representation as
     *                                  described in {@link #toString}
     */
    public static UUID fromString(final String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentFault(e.getMessage());
        }
    }

    /**
     * Compare function to sort a collection of time-based (version 1) UUIDs based on the timestamp.
     *
     * @param left left UUID
     * @param right right UUID
     * @return the value {@code 0} if {@code left timestamp == right timestamp};
     *         a value less than {@code 0} if {@code left timestamp < right timestamp}; and
     *         a value greater than {@code 0} if {@code left timestamp > right timestamp}
     * @throws IllegalArgumentException if either left or right are not a version 1 UUID
     */
    public static int compareByTime(UUID left, UUID right) {
        if (left.version() != 1) {
            throw new IllegalArgumentException(
                    String.format("Can only retrieve the unix timestamp for version 1 uuid (provided version %d)",
                                  left.version()));
        }
        if (right.version() != 1) {
            throw new IllegalArgumentException(
                    String.format("Can only retrieve the unix timestamp for version 1 uuid (provided version %d)",
                                  right.version()));
        }
        //
        long leftStamp = left.timestamp();
        long rightStamp = right.timestamp();
        return Long.compare(leftStamp, rightStamp);
    }

}
