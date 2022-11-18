package com.smartsparrow.eval.action.progress;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.eval.action.ActionContext;
import com.smartsparrow.util.DataType;

public class ProgressActionContext implements ActionContext<ProgressionType> {

    private ProgressionType progressionType;
    private UUID elementId;
    private CoursewareElementType elementType;

    public ProgressionType getProgressionType() {
        return progressionType;
    }

    public ProgressActionContext setProgressionType(ProgressionType progressionType) {
        this.progressionType = progressionType;
        return this;
    }

    public UUID getElementId() {
        return elementId;
    }

    public ProgressActionContext setElementId(UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    public CoursewareElementType getElementType() {
        return elementType;
    }

    public ProgressActionContext setElementType(CoursewareElementType elementType) {
        this.elementType = elementType;
        return this;
    }

    /**
     * Allows back-compatibility with existing actions. Required for Resolver
     */
    @Override
    public ProgressionType getValue() {
        return progressionType;
    }

    /**
     * This action only supports {@link com.smartsparrow.eval.action.resolver.ActionLiteralResolver}
     */
    @Override
    public DataType getDataType() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProgressActionContext that = (ProgressActionContext) o;
        return progressionType == that.progressionType &&
                Objects.equals(elementId, that.elementId) &&
                elementType == that.elementType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(progressionType, elementId, elementType);
    }

    @Override
    public String toString() {
        return "ProgressActionContext{" +
                "progressionType=" + progressionType +
                ", elementId=" + elementId +
                ", elementType=" + elementType +
                '}';
    }
}
