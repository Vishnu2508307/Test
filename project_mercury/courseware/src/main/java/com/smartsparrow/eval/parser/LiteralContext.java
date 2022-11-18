package com.smartsparrow.eval.parser;

import java.util.Objects;

import com.smartsparrow.eval.resolver.Resolver;

public class LiteralContext implements ResolverContext {

    private Resolver.Type type;

    @Override
    public Resolver.Type getType() {
        return type;
    }

    public LiteralContext setType(Resolver.Type type) {
        this.type = type;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LiteralContext that = (LiteralContext) o;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    @Override
    public String toString() {
        return "LiteralContext{" +
                "type=" + type +
                '}';
    }
}
