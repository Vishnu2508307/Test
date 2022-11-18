package com.smartsparrow.asset.service;

import com.smartsparrow.asset.data.AssetMediaType;
import com.smartsparrow.asset.data.AssetSource;

public interface BronteAssetResponse {

    AssetSource getAssetSource();
    AssetMediaType getAssetMediaType();
}
