package com.smartsparrow.graphql.type;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;

class GeneratorTest {

    private static final int len = 5;

    @InjectMocks
    private Generator generator;

    @BeforeEach
    void setup() {
        // create any @Mock
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @DisplayName("generate random string with a null pool")
    void generateRandomString_nullPool() {
        String actual = generator.randomString(len, null);

        assertThat(actual.length()).isEqualTo(len);
    }

    @Test
    @DisplayName("generate random string with a supplied pool")
    void generateRandomString_suppliedPool() {
        String actual = generator.randomString(len, Optional.of("abc"));

        assertThat(actual.length()).isEqualTo(len);
    }

    @Test
    @DisplayName("generate random string should throw on empty pool")
    void generateRandomString_emptyPool() {
        assertThrows(IllegalArgumentFault.class, //
                     () -> generator.randomString(len, Optional.of("")));
    }

    @Test
    @DisplayName("generate random string at min length")
    void generateRandomString_minLength() {
        String actual = generator.randomString(Generator.MIN_LENGTH, null);

        assertThat(actual.length()).isEqualTo(Generator.MIN_LENGTH);
    }

    @Test
    @DisplayName("generate random string should throw on minimum length minus 1")
    void generateRandomString_minLengthMinus1() {
        assertThrows(IllegalArgumentFault.class, //
                     () -> generator.randomString(Generator.MIN_LENGTH - 1, null));
    }

    @Test
    @DisplayName("generate random string at max length")
    void generateRandomString_maxLength() {
        String actual = generator.randomString(Generator.MAX_LENGTH, null);

        assertThat(actual.length()).isEqualTo(Generator.MAX_LENGTH);
    }

    @Test
    @DisplayName("generate random string should throw on max length plus 1")
    void generateRandomString_maxLengthPlusOne() {
        assertThrows(IllegalArgumentFault.class, //
                     () -> generator.randomString(Generator.MAX_LENGTH + 1, null));
    }
}
