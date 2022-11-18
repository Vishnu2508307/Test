package com.smartsparrow.math.data;

import java.util.Objects;
import java.util.UUID;

public class MathAssetResultNotification {

    private UUID notificationId;
    private String mathML;
    private UUID assetId;
    private String svgShape;

    public UUID getNotificationId() {
        return notificationId;
    }

    public MathAssetResultNotification setNotificationId(UUID notificationId) {
        this.notificationId = notificationId;
        return this;
    }

    public String getMathML() {
        return mathML;
    }

    public MathAssetResultNotification setMathML(String mathML) {
        this.mathML = mathML;
        return this;
    }

    public UUID getAssetId() {
        return assetId;
    }

    public MathAssetResultNotification setAssetId(UUID assetId) {
        this.assetId = assetId;
        return this;
    }

    public String getSvgShape() {
        return svgShape;
    }

    public MathAssetResultNotification setSvgShape(String svgShape) {
        this.svgShape = svgShape;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MathAssetResultNotification that = (MathAssetResultNotification) o;
        return Objects.equals(notificationId, that.notificationId) &&
                Objects.equals(mathML, that.mathML) &&
                Objects.equals(assetId, that.assetId) &&
                Objects.equals(svgShape, that.svgShape);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationId, mathML, assetId, svgShape);
    }

    @Override
    public String toString() {
        return "MathAssetRequestNotification{" +
                "notificationId=" + notificationId +
                ", mathML=" + mathML +
                ", assetId=" + assetId +
                ", svgShape=" + svgShape +
                '}';
    }
}
