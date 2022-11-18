package com.smartsparrow.eval.parser;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.smartsparrow.eval.resolver.Resolver;

public class ScopeContext implements ResolverContext {

    private Resolver.Type type;
    private UUID sourceId;
    private UUID studentScopeURN;
    private List<String> context;

    // this field is ignored in mercury
    private Map<String, Object> schemaProperty;
    private String category;

    @Override
    public Resolver.Type getType() {
        return type;
    }

    public ScopeContext setType(Resolver.Type type) {
        this.type = type;
        return this;
    }

    public UUID getSourceId() {
        return sourceId;
    }

    public ScopeContext setSourceId(UUID sourceId) {
        this.sourceId = sourceId;
        return this;
    }

    public UUID getStudentScopeURN() {
        return studentScopeURN;
    }

    public ScopeContext setStudentScopeURN(UUID studentScopeURN) {
        this.studentScopeURN = studentScopeURN;
        return this;
    }

    public List<String> getContext() {
        return context;
    }

    public ScopeContext setContext(List<String> context) {
        this.context = context;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getSchemaProperty() {
        return schemaProperty;
    }

    @JsonAnySetter
    public ScopeContext setSchemaProperty(Map<String, Object> schemaProperty) {
        this.schemaProperty = schemaProperty;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public ScopeContext setCategory(String category) {
        this.category = category;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScopeContext that = (ScopeContext) o;
        return type == that.type &&
                Objects.equals(sourceId, that.sourceId) &&
                Objects.equals(studentScopeURN, that.studentScopeURN) &&
                Objects.equals(context, that.context) &&
                Objects.equals(schemaProperty, that.schemaProperty) &&
                Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, sourceId, studentScopeURN, context, schemaProperty, category);
    }

    @Override
    public String toString() {
        return "ScopeContext{" +
                "type=" + type +
                ", sourceId=" + sourceId +
                ", studentScopeURN=" + studentScopeURN +
                ", context=" + context +
                ", schemaProperty=" + schemaProperty +
                ", category='" + category + '\'' +
                '}';
    }
}
