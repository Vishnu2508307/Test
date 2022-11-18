package com.smartsparrow.eval.action.score;

import java.util.Objects;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.deserializer.ResolverDeserializer;
import com.smartsparrow.eval.parser.ResolverContext;

public class ChangeScoreAction implements Action<ChangeScoreActionContext> {

    @JsonDeserialize(using = ResolverDeserializer.class)
    private ResolverContext resolver;

    private ChangeScoreActionContext context;

    private Double resolvedValue;

    @Override
    public Type getType() {
        return Type.CHANGE_SCORE;
    }

    @Override
    public ResolverContext getResolver() {
        return resolver;
    }

    @Override
    public ChangeScoreActionContext getContext() {
        return context;
    }

    @Override
    public Double getResolvedValue() {
        return resolvedValue;
    }

    @Override
    public ChangeScoreAction setResolvedValue(Object object) {
        this.resolvedValue = (Double) object;
        return this;
    }

    public ChangeScoreAction setResolver(ResolverContext resolver) {
        this.resolver = resolver;
        return this;
    }

    public ChangeScoreAction setContext(ChangeScoreActionContext context) {
        this.context = context;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChangeScoreAction that = (ChangeScoreAction) o;
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
        return "ChangeScoreAction{" +
                "resolver=" + resolver +
                ", context=" + context +
                ", resolvedValue=" + resolvedValue +
                '}';
    }
}
