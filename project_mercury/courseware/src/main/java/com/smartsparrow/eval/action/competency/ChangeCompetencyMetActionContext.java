package com.smartsparrow.eval.action.competency;

import java.util.UUID;

import com.google.common.base.Objects;
import com.smartsparrow.eval.action.ActionContext;
import com.smartsparrow.eval.mutation.MutationOperator;
import com.smartsparrow.util.DataType;

public class ChangeCompetencyMetActionContext implements ActionContext<Float> {

    private UUID documentId;
    private UUID documentItemId;
    private Float value;
    private MutationOperator operator;

    public UUID getDocumentId() {
        return documentId;
    }

    public ChangeCompetencyMetActionContext setDocumentId(UUID documentId) {
        this.documentId = documentId;
        return this;
    }

    public UUID getDocumentItemId() {
        return documentItemId;
    }

    public ChangeCompetencyMetActionContext setDocumentItemId(UUID documentItemId) {
        this.documentItemId = documentItemId;
        return this;
    }

    @Override
    public Float getValue() {
        return value;
    }

    @Override
    public DataType getDataType() {
        return DataType.NUMBER;
    }

    public ChangeCompetencyMetActionContext setValue(Float value) {
        this.value = value;
        return this;
    }

    public MutationOperator getOperator() {
        return operator;
    }

    public ChangeCompetencyMetActionContext setOperator(MutationOperator operator) {
        this.operator = operator;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ChangeCompetencyMetActionContext that = (ChangeCompetencyMetActionContext) o;
        return Objects.equal(getDocumentId(), that.getDocumentId()) && Objects
                .equal(getDocumentItemId(), that.getDocumentItemId()) && Objects.equal(getValue(), that.getValue())
                && getOperator() == that.getOperator();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getDocumentId(), getDocumentItemId(), getValue(), getOperator());
    }

    @Override
    public String toString() {
        return "ChangeCompetencyMetActionContext{"
                + "documentId=" + documentId
                + ", documentItemId=" + documentItemId
                + ", value=" + value
                + ", operator=" + operator
                + '}';
    }
}
