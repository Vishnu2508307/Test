package com.smartsparrow.asset.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.asset.data.AssetSignature;
import com.smartsparrow.asset.data.AssetSignatureConfiguration;
import com.smartsparrow.asset.data.AssetSignatureGateway;
import com.smartsparrow.asset.data.AssetSignatureStrategyType;
import com.smartsparrow.exception.IllegalArgumentFault;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class AssetSignatureServiceTest {

    @InjectMocks
    private AssetSignatureService assetSignatureService;

    @Mock
    private AssetSignatureGateway assetSignatureGateway;

    @Mock
    private AssetSignatureConfigurationDeserializer assetSignatureConfigurationDeserializer;

    private static final String url = "https://assets-bronte.pearson.com/acbd18db4cc2f85cedef654fccc4a4d8.jpg";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void signUrl_noConfig_noSignature() {
        when(assetSignatureGateway.findAssetSignature("assets-bronte.pearson.com", ""))
                .thenReturn(Mono.empty());

        String signedUrl = assetSignatureService.signUrl(url)
                .block();

        assertNotNull(signedUrl);
        assertEquals(url, signedUrl);
    }

    @Test
    void signUrl_akamaiTokenAuthSignature() {
        String key = "00a140670298f2ba50e7425375e168f9";
        long startTime = System.currentTimeMillis();
        long endTime = startTime + 50000L;
        final String akamaiConfig = "{" +
                "\"key\":\"" + key + "\"," +
                "\"tokenName\":\"aTokenName\"," +
                "\"startTime\":" + startTime + "," +
                "\"endTime\":" + endTime + "" +
                "}";
        AssetSignature akamaiSignature = new AssetSignature()
                .setId(UUID.randomUUID())
                .setConfig(akamaiConfig)
                .setAssetSignatureStrategyType(AssetSignatureStrategyType.AKAMAI_TOKEN_AUTHENTICATION);

        when(assetSignatureGateway.findAssetSignature("assets-bronte.pearson.com", ""))
                .thenReturn(Mono.just(akamaiSignature));
        when(assetSignatureConfigurationDeserializer
                .deserialize(eq(AssetSignatureStrategyType.AKAMAI_TOKEN_AUTHENTICATION), eq(akamaiConfig)))
                .thenCallRealMethod();

        String signedUrl = assetSignatureService.signUrl(url)
                .block();

        // example of what the signed url looks like
        // "https://assets-bronte.pearson.com/acbd18db4cc2f85cedef654fccc4a4d8.jpg?hdnts=st=1595805266286~exp=1595805316286~hmac=816a09a49b86cb3c368d95d302d3be66cd20b18e8be4fae6cbc342e276b8174e";

        assertNotNull(signedUrl);
        System.out.println(signedUrl);
        assertTrue(signedUrl.contains(url));
        assertTrue(signedUrl.contains("aTokenName"));
        assertTrue(signedUrl.contains("=st="));
        assertTrue(signedUrl.contains("~exp"));
        assertTrue(signedUrl.contains("~hmac="));
    }

    @Test
    void create_nullOrEmptyHost() {
        IllegalArgumentFault f1 = assertThrows(IllegalArgumentFault.class,
                () -> assetSignatureService.create(null, null, null, null)
                        .block());

        assertEquals("host is required", f1.getMessage());

        IllegalArgumentFault f2 = assertThrows(IllegalArgumentFault.class,
                () -> assetSignatureService.create("", null, null, null)
                        .block());


        assertEquals("host is required", f2.getMessage());

        verify(assetSignatureGateway, never()).persist(any(AssetSignature.class));
    }

    @Test
    void create_nullOrEmptyPath() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> assetSignatureService.create("host", null, null, null)
                        .block());


        assertEquals("path is required", f.getMessage());
    }

    @Test
    void create_nullOrEmptyConfig() {
        IllegalArgumentFault f1 = assertThrows(IllegalArgumentFault.class,
                () -> assetSignatureService.create("host", "path", null, null)
                        .block());

        assertEquals("config is required", f1.getMessage());

        IllegalArgumentFault f2 = assertThrows(IllegalArgumentFault.class,
                () -> assetSignatureService.create("host", "path", "", null)
                        .block());

        assertEquals("config is required", f2.getMessage());

        verify(assetSignatureGateway, never()).persist(any(AssetSignature.class));
    }

    @Test
    void create_nullType() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> assetSignatureService.create("host", "path", "config", null)
                        .block());

        assertEquals("assetSignatureType is required", f.getMessage());

        verify(assetSignatureGateway, never()).persist(any(AssetSignature.class));
    }

    @Test
    void create_invalidConfig() {

        final AssetSignatureStrategyType type = AssetSignatureStrategyType.AKAMAI_TOKEN_AUTHENTICATION;

        when(assetSignatureConfigurationDeserializer.deserialize(type, "config"))
                .thenCallRealMethod();

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> assetSignatureService.create("host", "path", "config", type)
                        .block());

        assertEquals(String.format("invalid configuration for %s", type), f.getMessage());

    }

    @Test
    void create() {
        final String config = "config";
        final AssetSignatureStrategyType type = AssetSignatureStrategyType.AKAMAI_TOKEN_AUTHENTICATION;
        AssetSignatureConfiguration assetSignatureConfiguration = mock(AssetSignatureConfiguration.class);
        ArgumentCaptor<AssetSignature> captor = ArgumentCaptor.forClass(AssetSignature.class);

        when(assetSignatureConfigurationDeserializer.deserialize(type, config)).thenReturn(assetSignatureConfiguration);
        when(assetSignatureGateway.persist(any(AssetSignature.class))).thenReturn(Flux.just(new Void[]{}));

        final AssetSignature created = assetSignatureService.create("host", "path", config, type)
                .block();

        verify(assetSignatureGateway).persist(captor.capture());

        final AssetSignature persisted = captor.getValue();

        assertAll(() -> {
            assertNotNull(created);
            assertNotNull(persisted);
            assertEquals(created, persisted);
            assertNotNull(persisted.getId());
            assertEquals("host", persisted.getHost());
            assertEquals("path", persisted.getPath());
            assertEquals("config", persisted.getConfig());
            assertEquals(type, persisted.getAssetSignatureStrategyType());
        });
    }

    @Test
    void delete_nullOrEmptyHost() {
        IllegalArgumentFault f1 = assertThrows(IllegalArgumentFault.class,
                () -> assetSignatureService.delete(null, "path")
                        .blockLast());

        assertEquals("host is required", f1.getMessage());

        IllegalArgumentFault f2 = assertThrows(IllegalArgumentFault.class,
                () -> assetSignatureService.delete("", "path")
                        .blockLast());

        assertEquals("host is required", f2.getMessage());

        verify(assetSignatureGateway, never()).delete(any(AssetSignature.class));
    }

    @Test
    void delete_nullOrEmptyPath() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> assetSignatureService.delete("host", null)
                        .blockLast());

        assertEquals("path is required", f.getMessage());
    }

    @Test
    void delete() {
        ArgumentCaptor<AssetSignature> captor = ArgumentCaptor.forClass(AssetSignature.class);

        when(assetSignatureGateway.delete(any(AssetSignature.class))).thenReturn(Flux.just(new Void[]{}));

        assetSignatureService.delete("host", "path")
                .blockLast();

        verify(assetSignatureGateway).delete(captor.capture());

        final AssetSignature deleted = captor.getValue();

        assertAll(() -> {
            assertNotNull(deleted);
            assertEquals("host", deleted.getHost());
            assertEquals("path", deleted.getPath());
        });
    }

}