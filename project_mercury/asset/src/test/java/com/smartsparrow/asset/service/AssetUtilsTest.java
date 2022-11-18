package com.smartsparrow.asset.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.smartsparrow.asset.data.AssetProvider;
import com.smartsparrow.asset.data.AssetSummary;
import com.smartsparrow.asset.data.AssetUrn;
import com.smartsparrow.asset.lang.AssetURNParseException;
import com.smartsparrow.asset.lang.AssetUrnBuildException;

class AssetUtilsTest {

    @Test
    void buildUrn_nullSummary() {
        assertThrows(AssetUrnBuildException.class, () -> AssetUtils.buildURN(null));
    }

    @Test
    void buildUrn_nullAssetId() {
        assertThrows(AssetUrnBuildException.class, () -> AssetUtils.buildURN(new AssetSummary()));
    }

    @Test
    void buildUrn_nullProvider() {
        AssetSummary summary = new AssetSummary()
                .setId(UUID.randomUUID());
        assertThrows(AssetUrnBuildException.class, () -> AssetUtils.buildURN(summary));
    }

    @Test
    void buildUrn_success() {
        AssetSummary summary = new AssetSummary()
                .setId(UUID.fromString("d2f78174-e273-4d84-88bd-24e186869da1"))
                .setProvider(AssetProvider.AERO);

        String urn = AssetUtils.buildURN(summary);

        assertEquals("urn:aero:d2f78174-e273-4d84-88bd-24e186869da1", urn);
    }

    @Test
    void parseURN_success(){
        String urn = "urn:aero:d2f78174-e273-4d84-88bd-24e186869da1";
        AssetUrn assetUrn = AssetUtils.parseURN(urn);
        assertEquals(assetUrn.getAssetProvider().getLabel(), "aero");
        assertEquals(assetUrn.getAssetId().toString(), "d2f78174-e273-4d84-88bd-24e186869da1");
    }

    @Test
    void parseURN_failure(){
        String urn = "urn:foo:d2f78174-e273-4d84-88bd-24e186869da1";
        assertThrows(AssetURNParseException.class, () -> AssetUtils.parseURN(urn));
    }
}
