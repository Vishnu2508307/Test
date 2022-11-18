package com.smartsparrow.asset.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.asset.data.BronteAssetContext;

public class BronteAssetEmptyTypeHandlerTest {

    @InjectMocks
    private BronteAssetEmptyTypeHandler handler;

    private final String assetUrn = "urn:aero:" + UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void test_handle_success() {
        BronteAssetEmptyResponse handle = handler.handle(new BronteAssetContext()
                                                               .setAssetUrn(assetUrn)).block();
        assertNotNull(handle);
    }
}
