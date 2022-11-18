package com.smartsparrow.asset.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.asset.data.AssetSource;
import com.smartsparrow.asset.data.BronteAssetContext;
import com.smartsparrow.asset.data.ImageSource;

import reactor.core.publisher.Mono;

class BronteAssetImageTypeHandlerTest {

    @InjectMocks
    private BronteAssetImageTypeHandler bronteAssetImageTypeHandler;

    @Mock
    private BronteImageAssetOptimizer bronteImageAssetOptimizer;

    @Mock
    private AssetSource source;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void optimize_image() {
        final UUID assetId = UUID.randomUUID();
        final ImageSource imageSource = mock(ImageSource.class);
        BronteAssetContext bronteAssetContext = new BronteAssetContext()
                .setAssetSource(imageSource);

        when(bronteImageAssetOptimizer.optimize(imageSource))
                .thenReturn(Mono.just(assetId));

        final BronteAssetImageResponse bronteAssetImageResult = bronteAssetImageTypeHandler.handle(bronteAssetContext)
                .block();

        assertNotNull(bronteAssetImageResult);
        assertEquals(assetId, bronteAssetImageResult.getAssetId());

        verify(bronteImageAssetOptimizer).optimize(eq(imageSource));
    }

}
