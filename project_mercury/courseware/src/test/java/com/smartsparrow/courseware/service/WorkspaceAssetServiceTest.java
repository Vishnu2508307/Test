package com.smartsparrow.courseware.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.asset.data.AssetGateway;
import com.smartsparrow.asset.data.AssetIdByUrn;
import com.smartsparrow.asset.data.AssetMetadata;
import com.smartsparrow.asset.data.AssetProvider;
import com.smartsparrow.asset.data.AssetSummary;
import com.smartsparrow.asset.service.AssetPayload;
import com.smartsparrow.asset.service.AssetService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class WorkspaceAssetServiceTest {

    private WorkspaceAssetService workspaceAssetService;

    @Mock
    private AssetService bronteAssetService;

    @Mock
    private AssetService alfrescoAssetService;

    @Mock
    private AssetService externalAssetService;

    @Mock
    private AssetGateway assetGateway;

    @Mock
    private AssetSummary assetSummary;

    private static final UUID assetId = UUID.randomUUID();
    private static final String assetUrn = "urn:aero:" + UUID.randomUUID();
    private static final String metadataKey = "altText";
    private static final String metadataValue = "small image";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        workspaceAssetService = new WorkspaceAssetService(
                bronteAssetService,
                alfrescoAssetService,
                externalAssetService,
                assetGateway
        );

        when(assetGateway.findAssetId(anyString()))
                .thenReturn(Mono.just(new AssetIdByUrn()
                        .setAssetId(assetId)));

        when(assetGateway.fetchAssetById(assetId))
                .thenReturn(Mono.just(assetSummary));
    }

    @Test
    void resolve_bronteAsset() {
        when(assetSummary.getProvider()).thenReturn(AssetProvider.AERO);

        final String urn = "urn:aero:a54e2680-6ad6-11eb-9135-e77a3641c2c5";

        when(bronteAssetService.getAssetPayload(assetId)).thenReturn(Mono.just(new AssetPayload()));

        AssetPayload payload = workspaceAssetService.getAssetPayload(urn)
                .block();

        assertNotNull(payload);
        verify(bronteAssetService).getAssetPayload(assetId);
        verify(externalAssetService, never()).getAssetPayload(assetId);
        verify(alfrescoAssetService, never()).getAssetPayload(assetId);
        verify(assetGateway).findAssetId(urn);
        verify(assetGateway).fetchAssetById(assetId);
    }

    @Test
    void resolve_alfrescoAsset() {
        when(assetSummary.getProvider()).thenReturn(AssetProvider.ALFRESCO);

        final String urn = "urn:alfresco:a54e2680-6ad6-11eb-9135-e77a3641c2c5";

        when(alfrescoAssetService.getAssetPayload(assetId)).thenReturn(Mono.just(new AssetPayload()));

        AssetPayload payload = workspaceAssetService.getAssetPayload(urn)
                .block();

        assertNotNull(payload);
        verify(alfrescoAssetService).getAssetPayload(assetId);
        verify(externalAssetService, never()).getAssetPayload(assetId);
        verify(bronteAssetService, never()).getAssetPayload(assetId);
        verify(assetGateway).findAssetId(urn);
        verify(assetGateway).fetchAssetById(assetId);
    }

    @Test
    void resolve_externalAsset() {
        when(assetSummary.getProvider()).thenReturn(AssetProvider.EXTERNAL);

        final String urn = "urn:aero:a54e2680-6ad6-11eb-9135-e77a3641c2c5";

        when(externalAssetService.getAssetPayload(assetId)).thenReturn(Mono.just(new AssetPayload()));

        AssetPayload payload = workspaceAssetService.getAssetPayload(urn)
                .block();

        assertNotNull(payload);
        verify(externalAssetService).getAssetPayload(assetId);
        verify(alfrescoAssetService, never()).getAssetPayload(assetId);
        verify(bronteAssetService, never()).getAssetPayload(assetId);
        verify(assetGateway).findAssetId(urn);
        verify(assetGateway).fetchAssetById(assetId);
    }

    @Test
    public void testUpdateAssetMetadata() {
        when(assetGateway.findAssetId(assetUrn)).thenReturn(Mono.just(new AssetIdByUrn()
                                                                              .setAssetId(assetId)
                                                                              .setAssetUrn(assetUrn)));
        when(assetGateway.persist(any(AssetMetadata.class))).thenReturn(Flux.just(new Void[]{}));

        AssetMetadata assetMetadata = workspaceAssetService.updateAssetMetadata(assetUrn,
                                                                                metadataKey,
                                                                                metadataValue).block();
        assertNotNull(assetMetadata);
        verify(assetGateway).findAssetId(assetUrn);
        verify(assetGateway).persist(any(AssetMetadata.class));
    }

}
