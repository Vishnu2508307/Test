package com.smartsparrow.math.service;

import java.util.UUID;

import com.smartsparrow.math.data.MathAssetErrorNotification;
import com.smartsparrow.math.data.MathAssetRequestNotification;
import com.smartsparrow.math.data.MathAssetResultNotification;
import com.smartsparrow.math.data.MathAssetRetryNotification;

public class MathAssetTestStub {

    public static MathAssetRequestNotification buildRequestNotification(final UUID assetId, final String mathML) {
        return new MathAssetRequestNotification()
                .setAssetId(assetId)
                .setmathML(mathML)
                .setNotificationId(UUID.randomUUID());
    }

    public static MathAssetResultNotification buildResultNotification(final MathAssetRequestNotification notification,
                                                                      final String svg) {
        return new MathAssetResultNotification()
                .setAssetId(notification.getAssetId())
                .setMathML(notification.getMathML())
                .setSvgShape(svg)
                .setNotificationId(notification.getNotificationId());
    }

    public static MathAssetErrorNotification buildErrorNotification(final String cause, final String error,
                                                                    final UUID notificationId, final UUID assetId) {
        return new MathAssetErrorNotification()
                .setCause(cause)
                .setErrorMessage(error)
                .setNotificationId(notificationId)
                .setAssetId(assetId);
    }

    public static MathAssetRetryNotification buildRetryNotification() {
        return new MathAssetRetryNotification()
                .setDelaySec(null)
                .setNotificationId(null)
                .setSourceNotificationId(null);
    }
}
