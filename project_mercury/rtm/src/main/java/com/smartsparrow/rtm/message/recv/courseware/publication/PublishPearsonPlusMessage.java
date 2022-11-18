package com.smartsparrow.rtm.message.recv.courseware.publication;

import com.smartsparrow.rtm.message.ReceivedMessage;

import java.util.Objects;
import java.util.UUID;

public class PublishPearsonPlusMessage extends ReceivedMessage {

    private UUID activityId;
    private String productId;

    public UUID getActivityId() {
        return activityId;
    }

    public PublishPearsonPlusMessage setActivityId(UUID activityId) {
        this.activityId = activityId;
        return this;
    }

    public String getProductId() {
        return productId;
    }

    public PublishPearsonPlusMessage setProductId(String productId) {
        this.productId = productId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublishPearsonPlusMessage that = (PublishPearsonPlusMessage) o;
        return Objects.equals(activityId, that.activityId) &&
                Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityId, productId);
    }

    @Override
    public String toString() {
        return "PublishClassOnDemandMessage{" +
                "activityId=" + activityId +
                ", productId='" + productId + '\'' +
                '}';
    }
}
