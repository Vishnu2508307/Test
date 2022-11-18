package com.smartsparrow.asset.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.asset.data.AssetErrorNotification;
import com.smartsparrow.asset.data.AssetGateway;
import com.smartsparrow.asset.data.AssetRequestNotification;
import com.smartsparrow.asset.data.AssetResultNotification;
import com.smartsparrow.asset.data.AssetRetryNotification;
import com.smartsparrow.asset.data.ImageSource;
import com.smartsparrow.asset.data.ImageSourceName;
import com.smartsparrow.asset.route.AssetRoute;
import com.smartsparrow.config.AssetConfig;
import com.smartsparrow.exception.IllegalArgumentFault;

import reactor.core.publisher.Flux;

class BronteImageAssetOptimizerTest {

    @InjectMocks
    private BronteImageAssetOptimizer bronteImageAssetOptimizer;

    @Mock
    private ProducerTemplate producerTemplate;

    @Mock
    private AssetGateway assetGateway;

    @Mock
    private AssetConfig assetConfig;

    @Mock
    private Exchange response;

    private static final UUID assetId = UUID.randomUUID();
    private static final Double height = 400.0;
    private static final Double width = 400.0;
    private static final Double threshold = ImageSourceName.MEDIUM.getThreshold();
    private static final String url = "http://asset.upload.example";
    private static final String size = ImageSourceName.MEDIUM.getLabel();

    private static final AssetRequestNotification requestNotification = AssetTestStub.buildRequestNotification(
            assetId, height, width, url, threshold, size
    );
    private static final AssetResultNotification resultNotification = AssetTestStub
            .buildResultNotification(requestNotification, "body");
    private static final AssetErrorNotification errorNotification = AssetTestStub
            .buildErrorNotification("cause", "error", requestNotification.getNotificationId(), assetId);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void processResultNotification_invalidArgs() {
        IllegalArgumentFault f1 = assertThrows(IllegalArgumentFault.class,
                () -> bronteImageAssetOptimizer.processResultNotification(null, null));

        assertNotNull(f1);
        assertEquals("resultNotification is required", f1.getMessage());

        IllegalArgumentFault f2 = assertThrows(IllegalArgumentFault.class,
                () -> bronteImageAssetOptimizer.processResultNotification(resultNotification, null));

        assertNotNull(f2);
        assertEquals("messagePayload is required", f2.getMessage());
    }

    @Test
    void processResultNotification() {
        when(assetGateway.persist(any(AssetResultNotification.class))).thenReturn(Flux.empty());
        when(assetGateway.persist(any(ImageSource.class))).thenReturn(Flux.empty());
        ArgumentCaptor<AssetResultNotification> captor = ArgumentCaptor.forClass(AssetResultNotification.class);
        final String payload = "{\"foo\":\"bar\"}";
        final AssetResultNotification res = bronteImageAssetOptimizer
                .processResultNotification(resultNotification, payload)
                .block();

        assertNotNull(res);
        assertNotNull(res.getHeight());
        assertNotNull(res.getWidth());
        assertNotNull(res.getUrl());
        assertNotNull(res.getSize());
        assertNotNull(res.getAssetId());

        verify(assetGateway).persist(captor.capture());

        final AssetResultNotification captured = captor.getValue();

        assertNotNull(captured);
        assertEquals(res, captured);
    }

    @Test
    void processErrorNotification_invalidArgs() {
        IllegalArgumentFault f1 = assertThrows(IllegalArgumentFault.class,
                () -> bronteImageAssetOptimizer.processErrorNotification(null, null));

        assertNotNull(f1);
        assertEquals("errorNotification is required", f1.getMessage());

        IllegalArgumentFault f2 = assertThrows(IllegalArgumentFault.class,
                () -> bronteImageAssetOptimizer.processErrorNotification(errorNotification, null));

        assertNotNull(f2);
        assertEquals("messagePayload is required", f2.getMessage());
    }

    @Test
    void processErrorNotification() {
        when(assetGateway.persist(any(AssetErrorNotification.class))).thenReturn(Flux.just(new Void[]{}));
        ArgumentCaptor<AssetErrorNotification> errorCaptor = ArgumentCaptor.forClass(AssetErrorNotification.class);
        final String payload = "{\"foo\":\"bar\"}";
        final AssetErrorNotification res = bronteImageAssetOptimizer
                .processErrorNotification(new AssetErrorNotification()
                        .setNotificationId(UUID.randomUUID()), payload)
                .block();

        assertNotNull(res);
        assertNull(res.getAssetId());

        verify(assetGateway).persist(errorCaptor.capture());

        final AssetErrorNotification capturedError = errorCaptor.getValue();

        assertNotNull(capturedError);
        assertNotNull(capturedError.getNotificationId());
    }

    @Test
    void processRetryNotification_invalidArgs() {
        IllegalArgumentFault f1 = assertThrows(IllegalArgumentFault.class,
                () -> bronteImageAssetOptimizer.processRetryNotification(null, null));

        assertNotNull(f1);
        assertEquals("retryNotification is required", f1.getMessage());

        IllegalArgumentFault f2 = assertThrows(IllegalArgumentFault.class,
                () -> bronteImageAssetOptimizer.processRetryNotification(new AssetRetryNotification(), null));

        assertNotNull(f2);
        assertEquals("messagePayload is required", f2.getMessage());
    }

    @Test
    void processRetryNotification() {
        when(assetGateway.persist(any(AssetRetryNotification.class))).thenReturn(Flux.empty());
        ArgumentCaptor<AssetRetryNotification> captor = ArgumentCaptor.forClass(AssetRetryNotification.class);

        final String payload = "{\"foo\":\"bar\"}";
        final AssetRetryNotification res = bronteImageAssetOptimizer
                .processRetryNotification(new AssetRetryNotification()
                        .setNotificationId(UUID.randomUUID()), payload)
                .block();

        assertNotNull(res);
        assertNotNull(res.getNotificationId());

        verify(assetGateway).persist(captor.capture());
    }

    @Test
    void sendResizeNotifications() {
        when(response.isFailed()).thenReturn(false);
        when(producerTemplate.request(eq(AssetRoute.SUBMIT_ASSET_RESIZE_REQUEST), any(Processor.class)))
                .thenReturn(response);
        when(assetGateway.persist(any(AssetRequestNotification.class))).thenReturn(Flux.empty());

        ImageSource image = new ImageSource()
                .setUrl(url)
                .setWidth(width)
                .setHeight(height)
                .setAssetId(assetId)
                .setName(ImageSourceName.ORIGINAL);

        bronteImageAssetOptimizer.optimize(image).block();

        verify(producerTemplate, times(2)).request(eq(AssetRoute.SUBMIT_ASSET_RESIZE_REQUEST), any(Processor.class));
    }

}