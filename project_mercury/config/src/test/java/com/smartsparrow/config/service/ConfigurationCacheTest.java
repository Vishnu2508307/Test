package com.smartsparrow.config.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.cache.Cache;

class ConfigurationCacheTest {

    private static final String key = "key";

    @Mock
    Cache<String, Object> cache;

    @InjectMocks
    ConfigurationCache configurationCache;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void get() throws Exception {
        when(cache.get(eq(key), any(Callable.class))).thenReturn("StringValue");

        String res = configurationCache.get(key, () -> "lol");
        assertEquals("StringValue", res);
        verify(cache).get(eq(key), any(Callable.class));
    }

    @Test
    void get_withExceptionException() throws Exception {
        when(cache.get(eq(key), any(Callable.class))).thenThrow(mock(ExecutionException.class));

        String res = configurationCache.get(key, () -> "lol");
        assertNull(res);
        verify(cache).get(eq(key), any(Callable.class));
    }
}
