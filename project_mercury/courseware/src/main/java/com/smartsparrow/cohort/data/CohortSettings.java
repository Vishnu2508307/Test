package com.smartsparrow.cohort.data;

import java.util.Objects;
import java.util.UUID;

public class CohortSettings {

    private UUID cohortId;
    private String bannerPattern;
    private String color;
    private String bannerImage;
    /**
     * this is in a urn format like -> x-urn:bronte:e6933083-95ec-4cb7-ae6b-ddd85e013826
     */
    private String productId;
    private UUID learnerRedirectId;

    public UUID getCohortId() {
        return cohortId;
    }

    public CohortSettings setCohortId(UUID cohortId) {
        this.cohortId = cohortId;
        return this;
    }

    public String getBannerPattern() {
        return bannerPattern;
    }

    public CohortSettings setBannerPattern(String bannerPattern) {
        this.bannerPattern = bannerPattern;
        return this;
    }

    public String getColor() {
        return color;
    }

    public CohortSettings setColor(String color) {
        this.color = color;
        return this;
    }

    public String getBannerImage() {
        return bannerImage;
    }

    public CohortSettings setBannerImage(String bannerImage) {
        this.bannerImage = bannerImage;
        return this;
    }

    /**
     * Get the Pearson Product ID; aka PDZ id, PPID
     * this is in a urn format like -> x-urn:bronte:e6933083-95ec-4cb7-ae6b-ddd85e013826
     *
     * @return the product id.
     */
    public String getProductId() {
        return productId;
    }

    public CohortSettings setProductId(String productId) {
        this.productId = productId;
        return this;
    }

    public UUID getLearnerRedirectId() {
        return learnerRedirectId;
    }

    public CohortSettings setLearnerRedirectId(UUID learnerRedirectId) {
        this.learnerRedirectId = learnerRedirectId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CohortSettings that = (CohortSettings) o;
        return Objects.equals(cohortId, that.cohortId) &&
                Objects.equals(bannerPattern, that.bannerPattern) &&
                Objects.equals(color, that.color) &&
                Objects.equals(bannerImage, that.bannerImage) &&
                Objects.equals(productId, that.productId) &&
                Objects.equals(learnerRedirectId, that.learnerRedirectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cohortId, bannerPattern, color, bannerImage, productId, learnerRedirectId);
    }

    @Override
    public String toString() {
        return "CohortSettings{" +
                "cohortId=" + cohortId +
                ", bannerPattern='" + bannerPattern + '\'' +
                ", color='" + color + '\'' +
                ", bannerImage='" + bannerImage + '\'' +
                ", productId='" + productId + '\'' +
                ", learnerRedirectId=" + learnerRedirectId +
                '}';
    }
}
