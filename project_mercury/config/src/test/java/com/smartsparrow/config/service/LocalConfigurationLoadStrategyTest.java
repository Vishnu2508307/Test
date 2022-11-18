package com.smartsparrow.config.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class LocalConfigurationLoadStrategyTest {

    @InjectMocks
    LocalConfigurationLoadStrategy localConfigurationLoadStrategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void load_success() {
        ConfigurationContext context = new ConfigurationContext()
                .setEnv("local")
                .setPrefix("test_infra")
                .setFileName("test_infra.json")
                .setKey("test.infra");

        TestConfig load = (TestConfig) localConfigurationLoadStrategy.load(context, TestConfig.class);
        assertNotNull(load);
        assertEquals("awsBucketName", load.getBucketName());
        assertEquals("awsBucketUrl", load.getBucketUrl());
    }
}
