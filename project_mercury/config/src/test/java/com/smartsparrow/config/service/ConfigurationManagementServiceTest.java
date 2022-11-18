package com.smartsparrow.config.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.config.data.ConfigurationGateway;
import com.smartsparrow.config.data.EnvConfiguration;

import reactor.core.publisher.Flux;

class ConfigurationManagementServiceTest {

    @Mock
    private ConfigurationGateway configurationGateway;

    @InjectMocks
    private ConfigurationManagementService configurationManagementService;

    private final static String REGION = "AP_SOUTHEAST_2";
    private final static String KEY = "key";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void fetchByRegion() {
        when(configurationGateway.fetchByRegion(REGION)).thenReturn(Flux.just(createConfig("key1", "value1")
                                                                            , createConfig("key2", "value2")));

        List<EnvConfiguration> result = configurationManagementService.fetchByRegion(REGION);

        assertAll(() -> {
            assertNotNull(result);
            assertEquals(2, result.size());
        });
    }

    @Test
    void fetchByRegionNoValues() {
        when(configurationGateway.fetchByRegion(REGION)).thenReturn(Flux.empty());

        List<EnvConfiguration> result = configurationManagementService.fetchByRegion(REGION);

        assertAll(() -> {
            assertNotNull(result);
            assertEquals(0, result.size());
        });
    }

    @Test
    void fetchByRegionAndKey() {
        when(configurationGateway.fetchByKeyAndRegion(KEY, REGION)).thenReturn(Flux.just(createConfig("key1", "value1")));

        EnvConfiguration result = configurationManagementService.fetchByRegionAndKey(REGION, KEY);

        assertAll(() -> {
            assertNotNull(result);
            assertEquals("key1", result.getKey());
            assertEquals("value1", result.getValue());
        });
    }

    @Test
    void persistConfiguration() {
        EnvConfiguration c = createConfig("key1", "value1");

        configurationManagementService.persistConfiguration(c);

        verify(configurationGateway, times(1)).persist(c);
    }

    private EnvConfiguration createConfig(String key, String value) {
        return new EnvConfiguration().setRegion(REGION).setKey(key).setValue(value);
    }
}
