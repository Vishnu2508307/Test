package com.smartsparrow.eval.action.scope;

import java.util.Objects;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.deserializer.ResolverDeserializer;
import com.smartsparrow.eval.parser.ResolverContext;

public class ChangeScopeAction implements Action<ChangeScopeActionContext> {

    private Action.Type type;

    @JsonDeserialize(using = ResolverDeserializer.class)
    private ResolverContext resolver;

    private ChangeScopeActionContext context;

    private Object resolvedValue;

    @Override
    public Type getType() {
        return type;
    }

    public ChangeScopeAction setType(Type type) {
        this.type = type;
        return this;
    }

    @Override
    public ResolverContext getResolver() {
        return resolver;
    }

    public ChangeScopeAction setResolver(ResolverContext resolver) {
        this.resolver = resolver;
        return this;
    }

    @Override
    public ChangeScopeActionContext getContext() {
        return context;
    }

    public ChangeScopeAction setContext(ChangeScopeActionContext context) {
        this.context = context;
        return this;
    }

    @Override
    public Object getResolvedValue() {
        return resolvedValue;
    }

    @Override
    public ChangeScopeAction setResolvedValue(Object resolvedValue) {
        this.resolvedValue = resolvedValue;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChangeScopeAction that = (ChangeScopeAction) o;
        return type == that.type &&
                Objects.equals(resolver, that.resolver) &&
                Objects.equals(context, that.context) &&
                Objects.equals(resolvedValue, that.resolvedValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, resolver, context, resolvedValue);
    }

    @Override
    public String toString() {
        return "ChangeScopeAction{" +
                "type=" + type +
                ", resolver=" + resolver +
                ", context=" + context +
                ", resolvedValue=" + resolvedValue +
                '}';
    }
}
