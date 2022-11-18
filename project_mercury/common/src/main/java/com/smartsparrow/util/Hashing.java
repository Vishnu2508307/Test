package com.smartsparrow.util;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Utility class to generate hashes consistently.
 */
public class Hashing {

    /**
     * Create a hash from the supplied email. Standardizes the input to lowercase.
     *
     * @param email the email
     * @return the string SHA-256 of the email
     */
    public static String email(final String email) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(email), "invalid email: " + email);

        // apply standardization
        final String normalizedEmail = Emails.normalize(email);

        return com.google.common.hash.Hashing.sha256().hashString(normalizedEmail, StandardCharsets.UTF_8).toString();
    }

    /**
     * Create a hash from the supplied {@link File} instance
     *
     * @param file the file to hash
     * @return the hash value
     */
    public static String file(File file) throws IOException {
        Preconditions.checkArgument(file != null, "file is null");
        return com.google.common.io.Files.asByteSource(file)
                .hash(com.google.common.hash.Hashing.murmur3_128())
                .toString();
    }

    public static String string(final String content) {
        affirmArgument(content != null, "content is required");
        return com.google.common.hash.Hashing.murmur3_128().hashString(content, StandardCharsets.UTF_8).toString();
    }
}
