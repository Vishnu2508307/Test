package com.smartsparrow.rtm.message.recv.courseware.publication;

import com.smartsparrow.cohort.service.LtiConsumerCredential;
import com.smartsparrow.rtm.message.ReceivedMessage;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Objects;
import java.util.UUID;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class PublishClassOnDemandMessage extends ReceivedMessage {

    private UUID activityId;
    private String productId;
    private String settings;
    private LtiConsumerCredential ltiConsumerCredential;

    public UUID getActivityId() {
        return activityId;
    }

    public PublishClassOnDemandMessage setActivityId(UUID activityId) {
        this.activityId = activityId;
        return this;
    }

    public String getProductId() {
        return productId;
    }

    public PublishClassOnDemandMessage setProductId(String productId) {
        this.productId = productId;
        return this;
    }

    public String getSettings() {
        return settings;
    }

    public PublishClassOnDemandMessage setSettings(String settings) {
        this.settings = settings;
        return this;
    }

    public LtiConsumerCredential getLtiConsumerCredential() {
        return ltiConsumerCredential;
    }

    public PublishClassOnDemandMessage setLtiConsumerCredential(LtiConsumerCredential ltiConsumerCredential) {
        this.ltiConsumerCredential = ltiConsumerCredential;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublishClassOnDemandMessage that = (PublishClassOnDemandMessage) o;
        return Objects.equals(activityId, that.activityId) &&
                Objects.equals(productId, that.productId) &&
                Objects.equals(settings, that.settings) &&
                Objects.equals(ltiConsumerCredential, that.ltiConsumerCredential);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityId, productId, ltiConsumerCredential);
    }

    @Override
    public String toString() {
        return "PublishClassOnDemandMessage{" +
                "activityId=" + activityId +
                ", productId='" + productId + '\'' +
                ", settings='" + settings + '\'' +
                ", ltiConsumerCredential='" + ltiConsumerCredential + '\'' +
                '}';
    }

}
