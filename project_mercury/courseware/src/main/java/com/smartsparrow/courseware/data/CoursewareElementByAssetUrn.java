package com.smartsparrow.courseware.data;

import java.util.Objects;

public class CoursewareElementByAssetUrn {

    private String assetUrn;
    private CoursewareElement coursewareElement;

    public String getAssetUrn() {
        return assetUrn;
    }

    public CoursewareElementByAssetUrn setAssetUrn(String assetUrn) {
        this.assetUrn = assetUrn;
        return this;
    }

    public CoursewareElement getCoursewareElement() {
        return coursewareElement;
    }

    public CoursewareElementByAssetUrn setCoursewareElement(CoursewareElement coursewareElement) {
        this.coursewareElement = coursewareElement;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoursewareElementByAssetUrn that = (CoursewareElementByAssetUrn) o;
        return Objects.equals(assetUrn, that.assetUrn) &&
                Objects.equals(coursewareElement, that.coursewareElement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetUrn, coursewareElement);
    }

    @Override
    public String toString() {
        return "CoursewareElementByAssetUrn{" +
                "urn='" + assetUrn + '\'' +
                ", coursewareElement=" + coursewareElement +
                '}';
    }
}
