package com.smartsparrow.eval.parser;

import java.util.Objects;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.smartsparrow.eval.deserializer.ResolverDeserializer;
import com.smartsparrow.eval.resolver.Resolvable;

public class Operand implements Resolvable<Object> {

    @JsonDeserialize(using = ResolverDeserializer.class)
    private ResolverContext resolver;

    private Object value;
    private Object resolvedValue;

    public ResolverContext getResolver() {
        return resolver;
    }

    public Operand setResolver(ResolverContext resolver) {
        this.resolver = resolver;
        return this;
    }

    public Object getValue() {
        return value;
    }

    public Operand setValue(Object value) {
        this.value = value;
        return this;
    }

    @Override
    public Object getResolvedValue() {
        return resolvedValue;
    }

    @Override
    public Operand setResolvedValue(Object resolvedValue) {
        this.resolvedValue = resolvedValue;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Operand operand = (Operand) o;
        return Objects.equals(resolver, operand.resolver) &&
                Objects.equals(value, operand.value) &&
                Objects.equals(resolvedValue, operand.resolvedValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resolver, value, resolvedValue);
    }

    @Override
    public String toString() {
        return "Operand{" +
                "resolver=" + resolver +
                ", value=" + value +
                ", resolvedValue=" + resolvedValue +
                '}';
    }
}
