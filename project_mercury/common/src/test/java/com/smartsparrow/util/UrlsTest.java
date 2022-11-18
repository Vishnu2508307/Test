package com.smartsparrow.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class UrlsTest {

    @Test
    void stripSlash() {
        assertEquals("string", Urls.stripSlash("/string"));
        assertEquals("string", Urls.stripSlash("/string/"));
        assertEquals("/string", Urls.stripSlash("//string/"));

        assertEquals(null, Urls.stripSlash(null));
    }

    @Test
    void concat() {
        assertEquals("string1/string2", Urls.concat("/string1", "string2"));

        assertEquals("string2", Urls.concat("", "string2"));
        assertEquals("string1", Urls.concat("string1", ""));
        assertEquals("", Urls.concat("", ""));

        assertEquals("string2", Urls.concat(null, "string2"));
        assertEquals("string1", Urls.concat("string1", null));
        assertEquals(null, Urls.concat(null, null));
    }
}
