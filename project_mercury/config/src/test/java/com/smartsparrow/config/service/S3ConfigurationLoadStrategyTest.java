package com.smartsparrow.config.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.amazonaws.services.acmpca.model.ResourceNotFoundException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;

import graphql.Assert;

public class S3ConfigurationLoadStrategyTest {

    @InjectMocks
    S3ConfigurationLoadStrategy s3ConfigurationLoadStrategy;
    @Mock
    AmazonS3 s3client;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void load_success() throws IOException {
        ConfigurationContext context = new ConfigurationContext()
                .setEnv("dev")
                .setRegion("ap-southeast-2")
                .setPrefix("test_infra")
                .setFileName("test_infra.json")
                .setKey("test.infra");

        S3Object s3Object = new S3Object();
        ClassLoader classLoader = getClass().getClassLoader();
        File file = load(classLoader, "local/test_infra/test_infra.json");
        assertNotNull(file);
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            s3Object.setObjectContent(fileInputStream);
            when(s3client.doesObjectExist(anyString(), anyString())).thenReturn(true);
            when(s3client.getObject(anyString(), anyString())).thenReturn(s3Object);
            TestConfig load = (TestConfig) s3ConfigurationLoadStrategy.load(context, TestConfig.class);
            assertNotNull(load);

            verify(s3client).doesObjectExist(anyString(), anyString());
            verify(s3client).getObject(anyString(), anyString());
        }
    }

    @Test
    void load_fileNotExist_error() {
        ConfigurationContext context = new ConfigurationContext()
                .setEnv("dev")
                .setRegion("ap-southeast-2")
                .setPrefix("test_infra")
                .setFileName("test_infra.json")
                .setKey("test.infra");

        when(s3client.doesObjectExist(anyString(), anyString())).thenReturn(false);
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                                                    () -> s3ConfigurationLoadStrategy.load(context, TestConfig.class));
        Assert.assertTrue(ex.getMessage().contains("S3 Configuration doesn't exist for"));
    }

    private File load(ClassLoader classLoader, String name) {
        URL url = classLoader.getResource(name);
        if (url != null) {
            return new File(url.getFile());
        }
        return null;
    }
}
