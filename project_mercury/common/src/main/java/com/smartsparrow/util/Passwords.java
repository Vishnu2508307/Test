package com.smartsparrow.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;

/**
 * Utility class to assist with the generation and validation of password hashes.
 *
 */
public class Passwords {

    // an OWASP article specifically dedicated to its Rfc2898DeriveBytes class. Specifically notes:
    // Using PBKDF2 for password storage, one should never output more bits than the base hash function's
    // size. With PBKDF2-SHA1 this is 160 bits or 20 bytes. Output more bits doesn't make the hash more secure,
    // but it costs the defender a lot more time while not costing the attacker. An attacker will just compare
    // the first hash function sized output saving them the time to generate the reset of the PBKDF2 output.
    private static final String ENCRYPTION_ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final int ENCRYPTION_ALGORITHM_KEY_LENGTH = 160;

    private static final SecureRandom generator = new SecureRandom();

    private Passwords() {
    }

    /**
     * Check if the hashed password is tainted and should be resaved.
     *
     * @param hash the password hash
     * @return true if the password is tainted, false otherwise.
     */
    public static boolean isTainted(String hash) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(hash), "hash can not be empty or null");

        // For example, this method would be further changed if:
        // a) the number of iterations changes
        // b) the underlying HMAC mechanism changes (to say scrypt)

        // check to ensure the number of iterations is up to date.
        String[] parts = toParts(hash);
        int hashIterations = Integer.parseInt(parts[3]);
        if (hashIterations < 32768) {
            return true;
        }

        return false;
    }

    /**
     * Hash the supplied password, using a random salt and default iterations.
     *
     * @param plainPassword the plain text password
     * @return a formatted HMAC
     */
    public static String hash(String plainPassword) {
        return hash(plainPassword, salt(16), 32768);
    }

    /**
     * Hash the supplied password using the supplied salt and default iterations.
     *
     * @param plainPassword the plain text password
     * @param salt the salt
     * @return a formatted HMAC
     */
    public static String hash(String plainPassword, byte[] salt) {
        return hash(plainPassword, salt, 32768);
    }

    /**
     * Hash the supplied password using the supplied salt and iterations.
     *
     * @param plainPassword the plain text password
     * @param salt the salt
     * @param iterations the number of times the PRF will be applied to the password when deriving the key
     * @return a formatted HMAC
     */
    public static String hash(String plainPassword, byte[] salt, int iterations) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(plainPassword), "password can not be empty or null");
        Preconditions.checkArgument(iterations >= 0, "the number of iterations must be positive");

        KeySpec spec = new PBEKeySpec(plainPassword.toCharArray(), salt, iterations, ENCRYPTION_ALGORITHM_KEY_LENGTH);
        SecretKeyFactory f;
        byte[] hash;
        try {
            f = SecretKeyFactory.getInstance(ENCRYPTION_ALGORITHM);
            hash = f.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }

        return String.format("PBKDF2:%s:%s:%d", encode(hash), encode(salt), iterations);
    }

    /**
     * Verify the supplied password matches the hash
     *
     * @param plainPassword the plain text password
     * @param hash the formatted HMAC hash
     * @return true, if the plain text password hashes to the supplied hash
     */
    public static boolean verify(String plainPassword, String hash) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(plainPassword));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(hash));
        String[] parts = toParts(hash);
        Preconditions.checkArgument(parts.length == 4);

        int hashIterations = Integer.parseInt(parts[3]);
        return hash(plainPassword, decode(parts[2]), hashIterations).equals(hash);
    }

    // generate random salt
    private static byte[] salt(int count) {
        byte[] salt = new byte[count];
        generator.nextBytes(salt);
        return salt;
    }

    // perform encoding of the bytes
    private static String encode(byte[] input) {
        return BaseEncoding.base16().lowerCase().encode(input);
    }

    // perform decoding of string
    private static byte[] decode(String input) {
        return BaseEncoding.base16().lowerCase().decode(input);
    }

    // split the supplied hash to its parts,
    // PBKDF2:hash:salt:iterations = ["PBKDF2", "hash", "salt", "iterations"]
    private static String[] toParts(String hash) {
        return hash.split(":");
    }
}
