package com.smartsparrow.asset.service;

import java.util.UUID;

import com.smartsparrow.asset.data.AssetProvider;
import com.smartsparrow.asset.data.AssetSummary;
import com.smartsparrow.asset.data.AssetUrn;
import com.smartsparrow.asset.lang.AssetURNParseException;
import com.smartsparrow.asset.lang.AssetUrnBuildException;

public class AssetUtils {

    /**
     * Parse URN and return the AssetUrn object which contains assetid and provider
     *
     * @param urn the asset URN
     * @return the asset id
     * @throws AssetURNParseException if urn can not be parsed
     */
    public static AssetUrn parseURN(String urn) {
        try {
          return new AssetUrn(urn);
        } catch (Throwable t) {
            throw new AssetURNParseException(urn, t);
        }
    }

    /**
     * Build a urn string from an asset summary object. The urn includes the {@link com.smartsparrow.asset.data.AssetProvider}
     * and the asset id with the following format: `urn:asset_provider:asset_id`. Example: <br>
     * `urn:aero:25cee900-2036-11e8-b2e7-37eaae35a819`
     *
     * @param assetSummary the summary to build the urn for
     * @return a string representing the asset summary urn
     * @throws AssetUrnBuildException if failing to build the urn for the given asset summary
     */
    public static String buildURN(AssetSummary assetSummary) {
        if (assetSummary == null || assetSummary.getProvider() == null || assetSummary.getId() == null) {
            throw new AssetUrnBuildException(assetSummary, "some of required parameters are null");
        }
        return String.format("urn:%s:%s", assetSummary.getProvider().getLabel(), assetSummary.getId());
    }
}
