package com.smartsparrow.cohort.data;

import java.util.Objects;
import java.util.UUID;

/**
 * This class is not used for now. Use {@link CohortSettings#bannerImage} instead.
 */
public class CohortBannerImage {

    public enum Size {
        SMALL,
        LARGE,
        MEDIUM,
        ORIGINAL;
    }

    private UUID cohortId;
    private Size size;
    private String bannerImage;

    public UUID getCohortId() {
        return cohortId;
    }

    public CohortBannerImage setCohortId(UUID cohortId) {
        this.cohortId = cohortId;
        return this;
    }

    public Size getSize() {
        return size;
    }

    public CohortBannerImage setSize(Size size) {
        this.size = size;
        return this;
    }

    public String getBannerImage() {
        return bannerImage;
    }

    public CohortBannerImage setBannerImage(String bannerImage) {
        this.bannerImage = bannerImage;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CohortBannerImage that = (CohortBannerImage) o;
        return Objects.equals(cohortId, that.cohortId) &&
                size == that.size &&
                Objects.equals(bannerImage, that.bannerImage);
    }

    @Override
    public int hashCode() {

        return Objects.hash(cohortId, size, bannerImage);
    }

    @Override
    public String toString() {
        return "CohortBannerImage{" +
                "cohortId=" + cohortId +
                ", size=" + size +
                ", bannerImage='" + bannerImage + '\'' +
                '}';
    }
}
