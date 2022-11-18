package com.smartsparrow.rtm.message.recv.cohort;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.cohort.data.EnrollmentType;
import com.smartsparrow.cohort.service.LtiConsumerCredential;
import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class ChangeCohortMessage extends ReceivedMessage implements CohortMessage {

    private UUID cohortId;
    private String name;
    private EnrollmentType enrollmentType;
    private String startDate;
    private String endDate;
    private String bannerImage;
    private String bannerPattern;
    private String color;
    private String productId;
    private UUID workspaceId;
    private LtiConsumerCredential ltiConsumerCredential;

    @Override
    public UUID getCohortId() {
        return cohortId;
    }

    public String getName() {
        return name;
    }

    public EnrollmentType getEnrollmentType() {
        return enrollmentType;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getBannerImage() {
        return bannerImage;
    }

    public String getBannerPattern() {
        return bannerPattern;
    }

    public String getColor() {
        return color;
    }

    public String getProductId() {
        return productId;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public ChangeCohortMessage setWorkspaceId(UUID workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public LtiConsumerCredential getLtiConsumerCredential() {
        return ltiConsumerCredential;
    }

    public ChangeCohortMessage setLtiConsumerCredential(LtiConsumerCredential ltiConsumerCredential) {
        this.ltiConsumerCredential = ltiConsumerCredential;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChangeCohortMessage that = (ChangeCohortMessage) o;
        return Objects.equals(cohortId, that.cohortId) &&
                Objects.equals(name, that.name) &&
                enrollmentType == that.enrollmentType &&
                Objects.equals(startDate, that.startDate) &&
                Objects.equals(endDate, that.endDate) &&
                Objects.equals(bannerImage, that.bannerImage) &&
                Objects.equals(bannerPattern, that.bannerPattern) &&
                Objects.equals(color, that.color) &&
                Objects.equals(productId, that.productId) &&
                Objects.equals(workspaceId, that.workspaceId) &&
                Objects.equals(ltiConsumerCredential, that.ltiConsumerCredential);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cohortId,
                            name,
                            enrollmentType,
                            startDate,
                            endDate,
                            bannerImage,
                            bannerPattern,
                            color,
                            productId,
                            workspaceId,
                            ltiConsumerCredential);
    }

    @Override
    public String toString() {
        return "ChangeCohortMessage{" +
                "cohortId=" + cohortId +
                ", name='" + name + '\'' +
                ", enrollmentType=" + enrollmentType +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", bannerImage='" + bannerImage + '\'' +
                ", bannerPattern='" + bannerPattern + '\'' +
                ", color='" + color + '\'' +
                ", productId='" + productId + '\'' +
                ", workspaceId='" + workspaceId + '\'' +
                ", ltiConsumerCredential='" + ltiConsumerCredential + '\'' +
                '}';
    }
}
