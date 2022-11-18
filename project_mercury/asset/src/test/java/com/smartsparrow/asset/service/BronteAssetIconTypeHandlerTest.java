package com.smartsparrow.asset.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.asset.data.AssetGateway;
import com.smartsparrow.asset.data.AssetSource;
import com.smartsparrow.asset.data.BronteAssetContext;
import com.smartsparrow.asset.data.IconSource;
import com.smartsparrow.asset.data.IconsByLibrary;
import com.smartsparrow.exception.IllegalArgumentFault;

import reactor.core.publisher.Flux;

public class BronteAssetIconTypeHandlerTest {

    @InjectMocks
    private BronteAssetIconTypeHandler bronteAssetIconTypeHandler;

    @Mock
    private AssetGateway assetGateway;

    @Mock
    private AssetSource source;
    private static final UUID assetId = UUID.randomUUID();
    private static final String assetUrn = "urn:aero:" + UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(assetGateway.persist(any(IconsByLibrary.class))).thenReturn(Flux.just(new Void[]{}));
    }

    @Test
    void test_handle_success() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("iconLibrary", "Microsoft icons");

        BronteAssetContext bronteAssetContext = new BronteAssetContext().setAssetSource(new IconSource()
                                                                                                .setAssetId(assetId))
                .setAssetUrn(assetUrn)
                .setMetadata(metadata);

        BronteAssetIconResponse handle = bronteAssetIconTypeHandler.handle(bronteAssetContext).block();
        assertNotNull(handle);
        verify(assetGateway).persist(any(IconsByLibrary.class));
    }

    @Test
    void test_handle_missing_iconLibrary() {
        Map<String, String> metadata = new HashMap<>();

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class,
                                               () -> bronteAssetIconTypeHandler.handle(new BronteAssetContext()
                                                                                               .setMetadata(metadata)
                                                                                               .setAssetUrn(assetUrn)));

        assertEquals("key iconLibrary is missing in the metadata", ex.getMessage());
    }
}
