package com.smartsparrow.eval.parser;

import java.util.Objects;

import com.smartsparrow.eval.operator.Operator;

public class BaseCondition implements Condition {

    private Type type;
    private Operator.Type operator;

    public BaseCondition setType(Type type) {
        this.type = type;
        return this;
    }

    public BaseCondition setOperator(Operator.Type operator) {
        this.operator = operator;
        return this;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Operator.Type getOperator() {
        return operator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseCondition that = (BaseCondition) o;
        return type == that.type &&
                operator == that.operator;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, operator);
    }

    @Override
    public String toString() {
        return "BaseCondition{\n" +
                "type=" + type + "\n" +
                ", operator=" + operator +
                "}\n";
    }
}
