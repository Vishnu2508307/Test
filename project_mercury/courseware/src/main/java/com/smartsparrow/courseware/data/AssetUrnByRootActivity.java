package com.smartsparrow.courseware.data;

import java.util.Objects;
import java.util.UUID;

public class AssetUrnByRootActivity {

    private UUID rootActivityId;
    private String assetUrn;
    private CoursewareElement coursewareElement;

    public UUID getRootActivityId() {
        return rootActivityId;
    }

    public AssetUrnByRootActivity setRootActivityId(UUID rootActivityId) {
        this.rootActivityId = rootActivityId;
        return this;
    }

    public String getAssetUrn() {
        return assetUrn;
    }

    public AssetUrnByRootActivity setAssetUrn(String assetUrn) {
        this.assetUrn = assetUrn;
        return this;
    }

    public CoursewareElement getCoursewareElement() {
        return coursewareElement;
    }

    public AssetUrnByRootActivity setCoursewareElement(CoursewareElement coursewareElement) {
        this.coursewareElement = coursewareElement;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetUrnByRootActivity that = (AssetUrnByRootActivity) o;
        return Objects.equals(rootActivityId, that.rootActivityId) &&
                Objects.equals(assetUrn, that.assetUrn) &&
                Objects.equals(coursewareElement, that.coursewareElement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rootActivityId, assetUrn, coursewareElement);
    }

    @Override
    public String toString() {
        return "AssetUrnByRootActivity{" +
                "rootActivityId=" + rootActivityId +
                ", assetUrn='" + assetUrn + '\'' +
                ", coursewareElement=" + coursewareElement +
                '}';
    }
}
