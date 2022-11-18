package com.smartsparrow.eval.action.score;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.eval.action.ActionContext;
import com.smartsparrow.eval.mutation.MutationOperator;
import com.smartsparrow.util.DataType;

public class ChangeScoreActionContext implements ActionContext<Double> {

    private Double value;
    private MutationOperator operator;
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

    public ChangeScoreActionContext setValue(Double value) {
        this.value = value;
        return this;
    }

    public MutationOperator getOperator() {
        return operator;
    }

    public ChangeScoreActionContext setOperator(MutationOperator operator) {
        this.operator = operator;
        return this;
    }

    public UUID getElementId() {
        return elementId;
    }

    public ChangeScoreActionContext setElementId(UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    public CoursewareElementType getElementType() {
        return elementType;
    }

    public ChangeScoreActionContext setElementType(CoursewareElementType elementType) {
        this.elementType = elementType;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChangeScoreActionContext that = (ChangeScoreActionContext) o;
        return Objects.equals(value, that.value) &&
                operator == that.operator &&
                Objects.equals(elementId, that.elementId) &&
                elementType == that.elementType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, operator, elementId, elementType);
    }

    @Override
    public String toString() {
        return "ChangeScoreActionContext{" +
                "value=" + value +
                ", operator=" + operator +
                ", elementId=" + elementId +
                ", elementType=" + elementType +
                '}';
    }
}
