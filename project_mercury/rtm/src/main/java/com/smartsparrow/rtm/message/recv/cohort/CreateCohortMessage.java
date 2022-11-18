package com.smartsparrow.rtm.message.recv.cohort;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.cohort.data.EnrollmentType;
import com.smartsparrow.cohort.service.LtiConsumerCredential;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.workspace.WorkspaceMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class CreateCohortMessage extends ReceivedMessage implements WorkspaceMessage {

    private String name;
    private EnrollmentType enrollmentType;
    private String startDate;
    private String endDate;
    private String bannerImage;
    private String bannerPattern;
    private String color;
    private UUID workspaceId;
    private String productId;
    private LtiConsumerCredential ltiConsumerCredential;

    public String getName() {
        return name;
    }

    public CreateCohortMessage setName(final String name) {
        this.name = name;
        return this;
    }

    public EnrollmentType getEnrollmentType() {
        return enrollmentType;
    }

    public CreateCohortMessage setEnrollmentType(final EnrollmentType enrollmentType) {
        this.enrollmentType = enrollmentType;
        return this;
    }

    public String getStartDate() {
        return startDate;
    }

    public CreateCohortMessage setStartDate(final String startDate) {
        this.startDate = startDate;
        return this;
    }

    public String getEndDate() {
        return endDate;
    }

    public CreateCohortMessage setEndDate(final String endDate) {
        this.endDate = endDate;
        return this;
    }

    public String getBannerImage() {
        return bannerImage;
    }

    public CreateCohortMessage setBannerImage(final String bannerImage) {
        this.bannerImage = bannerImage;
        return this;
    }

    public String getBannerPattern() {
        return bannerPattern;
    }

    public CreateCohortMessage setBannerPattern(final String bannerPattern) {
        this.bannerPattern = bannerPattern;
        return this;
    }

    public String getColor() {
        return color;
    }

    public CreateCohortMessage setColor(final String color) {
        this.color = color;
        return this;
    }

    public CreateCohortMessage setWorkspaceId(final UUID workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    @Override
    public UUID getWorkspaceId() {
        return workspaceId;
    }


    public String getProductId() {
        return productId;
    }

    public CreateCohortMessage setProductId(String productId) {
        this.productId = productId;
        return this;
    }

    public LtiConsumerCredential getLtiConsumerCredential() {
        return ltiConsumerCredential;
    }

    public CreateCohortMessage setLtiConsumerCredential(LtiConsumerCredential ltiConsumerCredential) {
        this.ltiConsumerCredential = ltiConsumerCredential;
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateCohortMessage that = (CreateCohortMessage) o;
        return Objects.equals(name, that.name) &&
                enrollmentType == that.enrollmentType &&
                Objects.equals(startDate, that.startDate) &&
                Objects.equals(endDate, that.endDate) &&
                Objects.equals(bannerImage, that.bannerImage) &&
                Objects.equals(bannerPattern, that.bannerPattern) &&
                Objects.equals(color, that.color) &&
                Objects.equals(workspaceId, that.workspaceId) &&
                Objects.equals(productId, that.productId) &&
                Objects.equals(ltiConsumerCredential, that.ltiConsumerCredential);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name,
                            enrollmentType,
                            startDate,
                            endDate,
                            bannerImage,
                            bannerPattern,
                            color,
                            workspaceId,
                            productId,
                            ltiConsumerCredential);
    }

    @Override
    public String toString() {
        return "CreateCohortMessage{" +
                "name='" + name + '\'' +
                ", enrollmentType=" + enrollmentType +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", bannerImage='" + bannerImage + '\'' +
                ", bannerPattern='" + bannerPattern + '\'' +
                ", color='" + color + '\'' +
                ", workspaceId=" + workspaceId +
                ", productId='" + productId + '\'' +
                ", ltiConsumerCredential='" + ltiConsumerCredential + '\'' +
                '}';
    }
}
