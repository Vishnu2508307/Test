package com.smartsparrow.asset.data;

import java.util.UUID;

public interface AssetSource {

    /**
     * @return the id of the asset this source belongs to
     */
    UUID getAssetId();
}
