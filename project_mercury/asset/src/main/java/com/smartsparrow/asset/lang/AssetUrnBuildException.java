package com.smartsparrow.asset.lang;

import com.smartsparrow.asset.data.AssetSummary;

public class AssetUrnBuildException extends RuntimeException {

    private static final String ERROR_MESSAGE = "cannot build urn for assetSummary %s %s";

    public AssetUrnBuildException(AssetSummary assetSummary, String message) {
        super(String.format(ERROR_MESSAGE, assetSummary, message));
    }
}
