package com.smartsparrow.rest.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;

import org.junit.jupiter.api.Test;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.BronteWebToken;
import com.smartsparrow.iam.service.WebSessionToken;

class CookiesTest {

    @Test
    void map() {
        final String expectedToken = "token1token";
        WebSessionToken webSessionToken = new WebSessionToken() //
                .setToken(expectedToken) //
                .setValidUntilTs(System.currentTimeMillis() + 86400000);

        NewCookie actual = Cookies.map(webSessionToken);

        assertEquals("bearerToken", actual.getName());
        assertEquals(expectedToken, actual.getValue());
        assertEquals("/", actual.getPath());
        assertEquals(".phx-spr.com", actual.getDomain());
        assertTrue(actual.getMaxAge() > 0);
    }

    @Test
    void mapToRemove() {
        Cookie existing = new Cookie("bearerToken", "token1token", "/", ".phx-spr.com");

        NewCookie actual = Cookies.mapToRemove(existing);

        assertEquals("bearerToken", actual.getName());
        assertEquals("", actual.getValue());
        assertEquals("/", actual.getPath());
        assertEquals(".phx-spr.com", actual.getDomain());
        assertTrue(actual.getMaxAge() <= 0);
    }

    @Test
    void map_null() {
        WebSessionToken webSessionToken = null;
        BronteWebToken bronteWebToken = null;
        assertThrows(IllegalArgumentFault.class, () -> Cookies.map(webSessionToken));
        assertThrows(IllegalArgumentFault.class, () -> Cookies.map(bronteWebToken));
    }

    @Test
    void mapToRemove_null() {
        assertThrows(IllegalArgumentFault.class, () -> Cookies.mapToRemove(null));
    }

}