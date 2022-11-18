package com.smartsparrow.config.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.smartsparrow.config.data.ConfigurationGateway;
import com.smartsparrow.config.data.EnvConfiguration;

import reactor.core.publisher.Flux;

class BaseConfigurationServiceTest {

    private BaseConfigurationService baseConfigurationService;
    private ConfigurationGateway configurationGateway;

    private static final String REGION = "AP_SOUTHEAST_2";
    private static final String KEY = "key";

    @BeforeEach
    void setUp() {
        configurationGateway = mock(ConfigurationGateway.class);
        baseConfigurationService = new BaseConfigurationService(configurationGateway, REGION);
    }

    @Test
    void getBoolean() {
        when(configurationGateway.fetchByKeyAndRegion(KEY, REGION)).thenReturn(Flux.just(configWithValue("true")));

        boolean result = baseConfigurationService.getBoolean(KEY);

        assertEquals(true, result);
    }

    @Test
    void getBooleanWithDefaultValue() {
        when(configurationGateway.fetchByKeyAndRegion(KEY, REGION)).thenReturn(Flux.empty());

        boolean result = baseConfigurationService.getBoolean(KEY, true);

        assertEquals(true, result);
    }

    @Test
    void getBooleanNoProperty() {
        when(configurationGateway.fetchByKeyAndRegion(KEY, REGION)).thenReturn(Flux.empty());

        boolean result = baseConfigurationService.getBoolean(KEY);

        assertEquals(false, result);
    }

    @Test
    void getInt() {
        when(configurationGateway.fetchByKeyAndRegion(KEY, REGION)).thenReturn(Flux.just(configWithValue("1111")));

        int result = baseConfigurationService.getInt(KEY);

        assertEquals(1111, result);
    }

    @Test
    void getIntDefaultValue() {
        when(configurationGateway.fetchByKeyAndRegion(KEY, REGION)).thenReturn(Flux.just(configWithValue("")));

        int result = baseConfigurationService.getInt(KEY, 100);

        assertEquals(100, result);
    }

    @Test
    void getIntNoValue() {
        when(configurationGateway.fetchByKeyAndRegion(KEY, REGION)).thenReturn(Flux.empty());

        int result = baseConfigurationService.getInt(KEY);

        assertEquals(0, result);
    }

    @Test
    void getInteger() {
        when(configurationGateway.fetchByKeyAndRegion(KEY, REGION)).thenReturn(Flux.just(configWithValue("1111")));

        Integer result = baseConfigurationService.getInteger(KEY);

        assertEquals(Integer.valueOf(1111), result);
    }

    @Test
    void getIntegerDefaultValue() {
        when(configurationGateway.fetchByKeyAndRegion(KEY, REGION)).thenReturn(Flux.empty());

        int result = baseConfigurationService.getInteger(KEY, 20);

        assertEquals(20, result);
    }

    @Test
    void getIntegerNoValue() {
        when(configurationGateway.fetchByKeyAndRegion(KEY, REGION)).thenReturn(Flux.empty());

        Integer result = baseConfigurationService.getInteger(KEY);

        assertEquals(null, result);
    }

    @Test
    void getString() {
        when(configurationGateway.fetchByKeyAndRegion(KEY, REGION)).thenReturn(Flux.just(configWithValue("test")));

        String result = baseConfigurationService.getString(KEY);

        assertEquals("test", result);
    }

    @Test
    void getStringDefaultValue() {
        when(configurationGateway.fetchByKeyAndRegion(KEY, REGION)).thenReturn(Flux.just(configWithValue("")));

        String result = baseConfigurationService.getString(KEY, "default");

        assertEquals("default", result);
    }

    @Test
    void getStringNoValue() {
        when(configurationGateway.fetchByKeyAndRegion(KEY, REGION)).thenReturn(Flux.empty());

        String result = baseConfigurationService.getString(KEY);

        assertEquals("", result);
    }

    @Test
    void get() {
        when(configurationGateway.fetchByKeyAndRegion(KEY, REGION))
                .thenReturn(Flux.just(configWithValue("{\"code\":\"value\",\"count\":5}")));

        TestConfig result = baseConfigurationService.getRenderedConfig(TestConfig.class, KEY);

        assertAll(() -> {
                    assertNotNull(result);
                    assertAll(() -> {
                        assertEquals("value", result.code);
                        assertEquals(5, result.count);
                    });
                }
        );
    }

    @Test
    void getNoValue() {
        when(configurationGateway.fetchByKeyAndRegion(KEY, REGION)).thenReturn(Flux.empty());

        TestConfig result = baseConfigurationService.getRenderedConfig(TestConfig.class, KEY);

        assertEquals(null, result);
    }

    @Test
    void getIncorrectObject() {
        when(configurationGateway.fetchByKeyAndRegion(KEY, REGION))
                .thenReturn(Flux.just(configWithValue("{\"code1\":\"value\",\"count\":5}")));

        TestConfig result = baseConfigurationService.getRenderedConfig(TestConfig.class, KEY);

        assertEquals(null, result);
    }


    @Test
    void getWithDefaultValue() {
        when(configurationGateway.fetchByKeyAndRegion(KEY, REGION)).thenReturn(Flux.empty());

        TestConfig result = baseConfigurationService.getRenderedConfig(TestConfig.class, KEY, new TestConfig().setCode("code1").setCount(1));

        assertAll(() -> {
                    assertNotNull(result);
                    assertAll(() -> {
                        assertEquals("code1", result.code);
                        assertEquals(1, result.count);
                    });
                }
        );
    }

    @Test
    void getValue() {
        when(configurationGateway.fetchByKeyAndRegion(KEY, REGION)).thenReturn(Flux.empty());

        assertNull(baseConfigurationService.getValue(KEY));
    }

    private EnvConfiguration configWithValue(String value) {
        EnvConfiguration config = new EnvConfiguration();
        config.setValue(value);
        return config;
    }

    private static class TestConfig {
        String code;
        int count;

        public TestConfig setCode(String code) {
            this.code = code;
            return this;
        }

        public TestConfig setCount(int count) {
            this.count = count;
            return this;
        }
    }
}
