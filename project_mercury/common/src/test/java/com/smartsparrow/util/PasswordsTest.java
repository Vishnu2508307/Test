package com.smartsparrow.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PasswordsTest {

    // Sample PBKDF2 hashes of "test"
    private static final String TEST_9 = "PBKDF2:6aa937920deea37a1a979faf1a66e14286e77dd9:397cea5b689e1efc612b8328eeacd125:9";
    private static final String TEST_32768 = "PBKDF2:47bb125335fed225fefccef60815899e98264ffe:1c32d9837f28a6c4b276a51372a6489b:32768";

    //
    private static final String plainText = "secretsecret";

    @Test
    void hash() {
        String hashed = Passwords.hash(plainText);

        assertThat(plainText).isNotEqualTo(hashed);
        assertThat(hashed).isNotNull();
        assertThat(hashed).contains(":32768");
        assertThat(4).isEqualTo(hashed.split(":").length);
    }

    @Test
    void salted() {
        String hashed1 = Passwords.hash(plainText);
        String hashed2 = Passwords.hash(plainText);

        assertThat(hashed1).isNotEqualTo(hashed2);
    }

    @Test
    void hash_null() {
        assertThrows(IllegalArgumentException.class, //
                     () -> Passwords.hash(null));
    }

    @Test
    void hash_empty() {
        assertThrows(IllegalArgumentException.class, //
                     () -> Passwords.hash(""));
    }

    @Test
    void hash_bad_interations() {
        assertThrows(IllegalArgumentException.class, //
                     () -> Passwords.hash("secret", new byte[1], -1701));
    }

    @Test
    void isTainted_yes() {
        assertThat(Passwords.isTainted(TEST_9)).isTrue();
    }

    @Test
    void isTainted_no() {
        assertThat(Passwords.isTainted(TEST_32768)).isFalse();
    }

    @Test
    void isTainted_null() {
        assertThrows(IllegalArgumentException.class, //
                     () -> Passwords.isTainted(null));
    }

    @Test
    void isTainted_empty() {
        assertThrows(IllegalArgumentException.class, //
                     () -> Passwords.isTainted(""));
    }

    @Test
    void verify() {
        assertThat(Passwords.verify("test", TEST_32768)).isTrue();
    }

    @Test
    void verify_non_default_iterations() {
        assertThat(Passwords.verify("test", TEST_9)).isTrue();
    }

    @Test
    void verify_bad_hash() {
        assertThrows(IllegalArgumentException.class, //
                     () -> Passwords.verify("test", "PBKDF2:hashsecret:0"));
    }
}
