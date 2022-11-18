package com.smartsparrow.eval.action.outcome;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.deserializer.ResolverDeserializer;
import com.smartsparrow.eval.parser.ResolverContext;

import java.util.Objects;

public class GradePassbackAction implements Action<GradePassbackActionContext> {

    @JsonDeserialize(using = ResolverDeserializer.class)
    private ResolverContext resolver;

    private GradePassbackActionContext context;

    private Double resolvedValue;

    @Override
    public Type getType() {
        return Type.GRADE_PASSBACK;
    }

    @Override
    public ResolverContext getResolver() {
        return resolver;
    }

    @Override
    public GradePassbackActionContext getContext() {
        return context;
    }

    @Override
    public Double getResolvedValue() {
        return resolvedValue;
    }

    @Override
    public GradePassbackAction setResolvedValue(Object object) {
        this.resolvedValue = (Double) object;
        return this;
    }

    public GradePassbackAction setResolver(ResolverContext resolver) {
        this.resolver = resolver;
        return this;
    }

    public GradePassbackAction setContext(GradePassbackActionContext context) {
        this.context = context;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GradePassbackAction that = (GradePassbackAction) o;
        return Objects.equals(resolver, that.resolver) &&
                Objects.equals(context, that.context) &&
                Objects.equals(resolvedValue, that.resolvedValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resolver, context, resolvedValue);
    }

    @Override
    public String toString() {
        return "GradePassbackAction{" +
                "resolver=" + resolver +
                ", context=" + context +
                ", resolvedValue=" + resolvedValue +
                '}';
    }
}
