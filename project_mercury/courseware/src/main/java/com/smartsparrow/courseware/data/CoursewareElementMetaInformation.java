package com.smartsparrow.courseware.data;

import java.util.Objects;
import java.util.UUID;

public class CoursewareElementMetaInformation {

    private String key;
    private String value;
    private UUID elementId;

    public String getKey() {
        return key;
    }

    public CoursewareElementMetaInformation setKey(final String key) {
        this.key = key;
        return this;
    }

    public String getValue() {
        return value;
    }

    public CoursewareElementMetaInformation setValue(final String value) {
        this.value = value;
        return this;
    }

    public UUID getElementId() {
        return elementId;
    }

    public CoursewareElementMetaInformation setElementId(final UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoursewareElementMetaInformation that = (CoursewareElementMetaInformation) o;
        return Objects.equals(key, that.key) &&
                Objects.equals(value, that.value) &&
                Objects.equals(elementId, that.elementId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value, elementId);
    }

    @Override
    public String toString() {
        return "CoursewareElementMetaInformation{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", elementId=" + elementId +
                '}';
    }
}
