package com.smartsparrow.courseware.data;

import java.util.Objects;
import java.util.UUID;

public class CoursewareElementConfigurationField extends ConfigurationField {

    private UUID elementId;

    public UUID getElementId() {
        return elementId;
    }

    public CoursewareElementConfigurationField setElementId(UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    @Override
    public CoursewareElementConfigurationField setFieldName(String fieldName) {
        super.setFieldName(fieldName);
        return this;
    }

    @Override
    public CoursewareElementConfigurationField setFieldValue(String fieldValue) {
        super.setFieldValue(fieldValue);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CoursewareElementConfigurationField that = (CoursewareElementConfigurationField) o;
        return Objects.equals(elementId, that.elementId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), elementId);
    }

    @Override
    public String toString() {
        return "CoursewareElementConfigurationField{" +
                "elementId=" + elementId +
                "} " + super.toString();
    }
}
