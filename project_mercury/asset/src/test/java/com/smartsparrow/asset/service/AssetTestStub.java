package com.smartsparrow.asset.service;

import java.util.UUID;

import com.smartsparrow.asset.data.AssetErrorNotification;
import com.smartsparrow.asset.data.AssetRequestNotification;
import com.smartsparrow.asset.data.AssetResultNotification;
import com.smartsparrow.asset.data.AssetRetryNotification;
import com.smartsparrow.asset.data.ImageSourceName;

public class AssetTestStub {

    public static AssetRequestNotification buildRequestNotification(final UUID assetId, final Double height,
                                                                    final Double width, final String url,
                                                                    final Double threshold, final String size) {
        return new AssetRequestNotification()
                .setAssetId(assetId)
                .setOriginalHeight(height)
                .setOriginalWidth(width)
                .setNotificationId(UUID.randomUUID())
                .setUrl(url)
                .setThreshold(threshold)
                .setSize(size);
    }

    public static AssetResultNotification buildResultNotification(final AssetRequestNotification notification,
                                                                  final String body) {
        return new AssetResultNotification()
                .setAssetId(notification.getAssetId())
                .setHeight(notification.getOriginalHeight())
                .setWidth(notification.getOriginalWidth())
                .setSize(ImageSourceName.MEDIUM.getLabel())
                .setNotificationId(notification.getNotificationId())
                .setUrl(notification.getUrl());
    }

    public static AssetErrorNotification buildErrorNotification(final String cause, final String error,
                                                                final UUID notificationId, final UUID assetId) {
        return new AssetErrorNotification()
                .setCause(cause)
                .setErrorMessage(error)
                .setNotificationId(notificationId)
                .setAssetId(assetId);
    }

    public static AssetRetryNotification buildRetryNotification() {
        return new AssetRetryNotification()
                .setDelaySec(null)
                .setNotificationId(null)
                .setSourceNotificationId(null);
    }
}
