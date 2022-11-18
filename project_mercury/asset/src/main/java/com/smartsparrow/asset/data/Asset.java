package com.smartsparrow.asset.data;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.leangen.graphql.annotations.types.GraphQLInterface;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "assetProvider",
        defaultImpl = BronteAsset.class
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BronteAsset.class, name = "AERO"),
        @JsonSubTypes.Type(value = ExternalAsset.class, name = "EXTERNAL"),
        @JsonSubTypes.Type(value = AlfrescoAsset.class, name = "ALFRESCO"),
        @JsonSubTypes.Type(value = MathAsset.class, name = "MATH")
})
@GraphQLInterface(name = "Asset", implementationAutoDiscovery = true)
public interface Asset {

    UUID getId();

    AssetProvider getAssetProvider();

    AssetMediaType getAssetMediaType();

    AssetVisibility getAssetVisibility();

    UUID getOwnerId();

    UUID getSubscriptionId();
}
