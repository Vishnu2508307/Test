package com.smartsparrow.eval.action.feedback;

import java.util.Objects;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.deserializer.ResolverDeserializer;
import com.smartsparrow.eval.parser.ResolverContext;

public class SendFeedbackAction implements Action<SendFeedbackActionContext> {

    private Type type;

    private SendFeedbackActionContext context;

    @JsonDeserialize(using = ResolverDeserializer.class)
    private ResolverContext resolver;

    private Object resolvedValue;

    @Override
    public Type getType() {
        return type;
    }

    public SendFeedbackAction setType(Type type) {
        this.type = type;
        return this;
    }

    @Override
    public SendFeedbackActionContext getContext() {
        return context;
    }

    public SendFeedbackAction setContext(SendFeedbackActionContext context) {
        this.context = context;
        return this;
    }

    @Override
    public ResolverContext getResolver() {
        return resolver;
    }

    public SendFeedbackAction setResolver(ResolverContext resolver) {
        this.resolver = resolver;
        return this;
    }

    @Override
    public Object getResolvedValue() {
        return resolvedValue;
    }

    @Override
    public SendFeedbackAction setResolvedValue(Object resolvedValue) {
        this.resolvedValue = resolvedValue;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SendFeedbackAction that = (SendFeedbackAction) o;
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
        return "SendFeedbackAction{" +
                "type=" + type +
                ", context=" + context +
                ", resolver=" + resolver +
                ", resolvedValue=" + resolvedValue +
                '}';
    }
}
