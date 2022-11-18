package com.smartsparrow.eval.action.competency;

import java.util.Objects;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.deserializer.ResolverDeserializer;
import com.smartsparrow.eval.parser.ResolverContext;

public class ChangeCompetencyMetAction implements Action<ChangeCompetencyMetActionContext> {

    private Type type;

    private ChangeCompetencyMetActionContext context;

    @JsonDeserialize(using = ResolverDeserializer.class)
    private ResolverContext resolver;

    private Object resolvedValue;

    @Override
    public Type getType() {
        return type;
    }

    public ChangeCompetencyMetAction setType(Type type) {
        this.type = type;
        return this;
    }

    @Override
    public ChangeCompetencyMetActionContext getContext() {
        return context;
    }

    public ChangeCompetencyMetAction setContext(ChangeCompetencyMetActionContext context) {
        this.context = context;
        return this;
    }

    @Override
    public ResolverContext getResolver() {
        return resolver;
    }

    public ChangeCompetencyMetAction setResolver(ResolverContext resolver) {
        this.resolver = resolver;
        return this;
    }

    @Override
    public Object getResolvedValue() {
        return resolvedValue;
    }

    @Override
    public ChangeCompetencyMetAction setResolvedValue(Object resolvedValue) {
        this.resolvedValue = resolvedValue;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChangeCompetencyMetAction that = (ChangeCompetencyMetAction) o;
        return type == that.type &&
                Objects.equals(context, that.context) &&
                Objects.equals(resolver, that.resolver) &&
                Objects.equals(resolvedValue, that.resolvedValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, context, resolver, resolvedValue);
    }

    @Override
    public String toString() {
        return "ChangeCompetencyMetAction{" +
                "type=" + type +
                ", context=" + context +
                ", resolver=" + resolver +
                ", resolvedValue=" + resolvedValue +
                '}';
    }
}
