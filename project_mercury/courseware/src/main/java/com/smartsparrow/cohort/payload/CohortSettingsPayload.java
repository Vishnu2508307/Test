package com.smartsparrow.cohort.payload;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smartsparrow.cohort.data.CohortSettings;
import com.smartsparrow.cohort.service.LtiConsumerCredential;

public class CohortSettingsPayload {

    private String bannerImage;
    private String bannerPattern;
    private String color;
    private String productId;
    private List<LtiConsumerCredential> ltiConsumerCredentials;

    @JsonIgnore
    public static CohortSettingsPayload from(@Nonnull CohortSettings cohortSettings) {
        checkNotNull(cohortSettings);
        CohortSettingsPayload cohortSettingsPayload = new CohortSettingsPayload();
        cohortSettingsPayload.bannerImage = cohortSettings.getBannerImage();
        cohortSettingsPayload.bannerPattern = cohortSettings.getBannerPattern();
        cohortSettingsPayload.color = cohortSettings.getColor();
        cohortSettingsPayload.productId = cohortSettings.getProductId();
        return cohortSettingsPayload;
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

    public CohortSettingsPayload setBannerImage(String bannerImage) {
        this.bannerImage = bannerImage;
        return this;
    }

    public CohortSettingsPayload setBannerPattern(String bannerPattern) {
        this.bannerPattern = bannerPattern;
        return this;
    }

    public CohortSettingsPayload setColor(String color) {
        this.color = color;
        return this;
    }

    public CohortSettingsPayload setProductId(String productId) {
        this.productId = productId;
        return this;
    }

    public String getProductId() {
        return productId;
    }

    public List<LtiConsumerCredential> getLtiConsumerCredentials() {
        return ltiConsumerCredentials;
    }

    public CohortSettingsPayload setLtiConsumerCredentials(List<LtiConsumerCredential> ltiConsumerCredentials) {
        this.ltiConsumerCredentials = ltiConsumerCredentials;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CohortSettingsPayload that = (CohortSettingsPayload) o;
        return Objects.equals(bannerImage, that.bannerImage) &&
                Objects.equals(bannerPattern, that.bannerPattern) &&
                Objects.equals(color, that.color) &&
                Objects.equals(productId, that.productId) &&
                Objects.equals(ltiConsumerCredentials, that.ltiConsumerCredentials);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bannerImage,
                            bannerPattern,
                            color,
                            productId,
                            ltiConsumerCredentials);
    }

    @Override
    public String toString() {
        return "CohortSettingsPayload{" +
                "bannerImage='" + bannerImage + '\'' +
                ", bannerPattern='" + bannerPattern + '\'' +
                ", color='" + color + '\'' +
                ", productId=" + productId +
                ", ltiConsumerCredentials=" + ltiConsumerCredentials +
                '}';

    }
}
