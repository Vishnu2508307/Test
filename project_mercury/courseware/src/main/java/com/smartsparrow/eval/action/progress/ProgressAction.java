package com.smartsparrow.eval.action.progress;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.deserializer.ResolverDeserializer;
import com.smartsparrow.eval.parser.ResolverContext;
import com.smartsparrow.exception.UnsupportedOperationFault;

public class ProgressAction implements Action<ProgressActionContext> {

    private Type type;

    private ProgressActionContext context;

    @JsonDeserialize(using = ResolverDeserializer.class)
    private ResolverContext resolver;

    private Object resolvedValue;

    @Override
    public Type getType() {
        return type;
    }

    public ProgressAction setType(Type type) {
        this.type = type;
        return this;
    }

    @Override
    public ProgressActionContext getContext() {
        return context;
    }

    public ProgressAction setContext(ProgressActionContext context) {
        this.context = context;
        return this;
    }

    @Override
    public ResolverContext getResolver() {
        return resolver;
    }

    public ProgressAction setResolver(ResolverContext resolver) {
        this.resolver = resolver;
        return this;
    }

    @Override
    public Object getResolvedValue() {
        return resolvedValue;
    }

    @Override
    public ProgressAction setResolvedValue(Object resolvedValue) {
        this.resolvedValue = resolvedValue;
        return this;
    }

    @JsonIgnore
    public boolean isInteractiveCompleted() {
        return context.getProgressionType().interactiveCompleted();
    }

    @JsonIgnore
    public boolean isActivityCompleted() {
        return context.getProgressionType().activityCompleted();
    }

    @JsonIgnore
    public boolean isWalkableCompleted() {
        return context.getProgressionType().walkableCompleted();
    }

    @JsonIgnore
    public boolean isWalkableCompleted(CoursewareElementType walkableType) {
        switch (walkableType) {
            case INTERACTIVE:
                return isInteractiveCompleted();
            case ACTIVITY:
                return isActivityCompleted();
            default:
                throw new UnsupportedOperationFault(String.format("elementType %s not a walkable type", walkableType));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProgressAction that = (ProgressAction) o;
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
        return "ProgressAction{" +
                "type=" + type +
                ", context=" + context +
                ", resolver=" + resolver +
                ", resolvedValue=" + resolvedValue +
                '}';
    }
}
