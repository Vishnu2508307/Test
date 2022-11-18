package com.smartsparrow.eval.action.outcome;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.eval.action.ActionContext;
import com.smartsparrow.util.DataType;

import java.util.Objects;
import java.util.UUID;

public class GradePassbackActionContext implements ActionContext<Double> {

    private Double value;
    private UUID elementId;
    private CoursewareElementType elementType;

    @Override
    public Double getValue() {
        return value;
    }

    @Override
    public DataType getDataType() {
        return DataType.NUMBER;
    }

    public GradePassbackActionContext setValue(Double value) {
        this.value = value;
        return this;
    }

    public UUID getElementId() {
        return elementId;
    }

    public GradePassbackActionContext setElementId(UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    public CoursewareElementType getElementType() {
        return elementType;
    }

    public GradePassbackActionContext setElementType(CoursewareElementType elementType) {
        this.elementType = elementType;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GradePassbackActionContext that = (GradePassbackActionContext) o;
        return Objects.equals(value, that.value) &&
                Objects.equals(elementId, that.elementId) &&
                elementType == that.elementType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, elementId, elementType);
    }

    @Override
    public String toString() {
        return "GradePassbackActionContext{" +
                "value=" + value +
                ", elementId=" + elementId +
                ", elementType=" + elementType +
                '}';
    }
}
