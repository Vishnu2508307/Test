package com.smartsparrow.eval.action.unsupported;

import java.util.Objects;

import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.parser.LiteralContext;
import com.smartsparrow.eval.parser.ResolverContext;
import com.smartsparrow.eval.resolver.Resolvable;
import com.smartsparrow.eval.resolver.Resolver;

public class UnsupportedAction implements Action<UnsupportedActionContext> {

    private UnsupportedActionContext context;

    @Override
    public Type getType() {
        return Type.UNSUPPORTED_ACTION;
    }

    @Override
    public ResolverContext getResolver() {
        return new LiteralContext()
                .setType(Resolver.Type.LITERAL);
    }

    @Override
    public UnsupportedActionContext getContext() {
        return context;
    }

    public UnsupportedAction setContext(UnsupportedActionContext context) {
        this.context = context;
        return this;
    }

    @Override
    public Object getResolvedValue() {
        return null;
    }

    @Override
    public Resolvable setResolvedValue(Object object) {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnsupportedAction that = (UnsupportedAction) o;
        return Objects.equals(context, that.context);
    }

    @Override
    public int hashCode() {
        return Objects.hash(context);
    }

    @Override
    public String toString() {
        return "UnsupportedAction{" +
                "context=" + context +
                '}';
    }
}
