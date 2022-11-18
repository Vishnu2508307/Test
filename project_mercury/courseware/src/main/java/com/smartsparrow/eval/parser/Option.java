package com.smartsparrow.eval.parser;

import java.util.Objects;

public class Option {

    public enum Type {
        IGNORE_CASE,
        DECIMAL
    }

    private Type type;
    private Object value;

    public Type getType() {
        return type;
    }

    public Option setType(Type type) {
        this.type = type;
        return this;
    }

    public Object getValue() {
        return value;
    }

    public Option setValue(Object value) {
        this.value = value;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Option option = (Option) o;
        return type == option.type &&
                Objects.equals(value, option.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    @Override
    public String toString() {
        return "Option{" +
                "type=" + type +
                ", value=" + value +
                '}';
    }
}
