package com.smartsparrow.eval.action.scope;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.smartsparrow.eval.action.ActionContext;
import com.smartsparrow.eval.mutation.MutationOperator;
import com.smartsparrow.util.DataType;

public class ChangeScopeActionContext implements ActionContext<Object> {

    private UUID sourceId;
    private UUID studentScopeURN;
    private DataType dataType;
    private MutationOperator operator;
    private List<String> context;
    private Object value;
    private Map<String, String> schemaProperty;

    public UUID getSourceId() {
        return sourceId;
    }

    public ChangeScopeActionContext setSourceId(UUID sourceId) {
        this.sourceId = sourceId;
        return this;
    }

    public UUID getStudentScopeURN() {
        return studentScopeURN;
    }

    public ChangeScopeActionContext setStudentScopeURN(UUID studentScopeURN) {
        this.studentScopeURN = studentScopeURN;
        return this;
    }

    @Override
    public DataType getDataType() {
        return dataType;
    }

    public ChangeScopeActionContext setDataType(DataType dataType) {
        this.dataType = dataType;
        return this;
    }

    public MutationOperator getOperator() {
        return operator;
    }

    public ChangeScopeActionContext setOperator(MutationOperator operator) {
        this.operator = operator;
        return this;
    }

    public List<String> getContext() {
        return context;
    }

    public ChangeScopeActionContext setContext(List<String> context) {
        this.context = context;
        return this;
    }

    @Override
    public Object getValue() {
        return value;
    }

    public ChangeScopeActionContext setValue(Object value) {
        this.value = value;
        return this;
    }

    @JsonAnyGetter
    public Map<String, String> getSchemaProperty() {
        return schemaProperty;
    }

    @JsonAnySetter
    public ChangeScopeActionContext setSchemaProperty(Map<String, String> schemaProperty) {
        this.schemaProperty = schemaProperty;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChangeScopeActionContext that = (ChangeScopeActionContext) o;
        return Objects.equals(sourceId, that.sourceId) &&
                Objects.equals(studentScopeURN, that.studentScopeURN) &&
                dataType == that.dataType &&
                operator == that.operator &&
                Objects.equals(context, that.context) &&
                Objects.equals(value, that.value) &&
                Objects.equals(schemaProperty, that.schemaProperty);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceId, studentScopeURN, dataType, operator, context, value, schemaProperty);
    }

    @Override
    public String toString() {
        return "ChangeScopeActionContext{" +
                "sourceId=" + sourceId +
                ", studentScopeURN=" + studentScopeURN +
                ", dataType=" + dataType +
                ", operator=" + operator +
                ", context=" + context +
                ", value=" + value +
                ", schemaProperty=" + schemaProperty +
                '}';
    }
}
