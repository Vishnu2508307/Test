package com.smartsparrow.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class HashingTest {

    @Test
    void email() {
        String actual = Hashing.email("someone@domain.tld");
        assertThat(actual).isEqualTo("0842c12b252ad804115e9c6f03bb2c17422182292323eb319991b93ed40a98ac");
    }

    @Test
    void email_empty() {
        assertThrows(IllegalArgumentException.class, //
                () -> Hashing.email(""));
    }

    @Test
    void email_null() {
        assertThrows(IllegalArgumentException.class, //
                () -> Hashing.email(null));
    }

    @Test
    void inputStream_null() {
        assertThrows(IllegalArgumentException.class,
                ()-> Hashing.file(null));
    }

    @Test
    void inputStream() throws IOException {
        Path tempFilePath = Files.createTempFile(null, null);
        File file = new File(tempFilePath.toString());
        assertNotNull(Hashing.file(file));
        Files.delete(tempFilePath);
    }

}
