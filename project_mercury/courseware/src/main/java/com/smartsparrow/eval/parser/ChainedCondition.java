package com.smartsparrow.eval.parser;

import java.util.List;
import java.util.Objects;

import com.smartsparrow.eval.operator.Operator;

public class ChainedCondition extends BaseCondition {

    private List<? extends BaseCondition> conditions;

    public List<? extends BaseCondition> getConditions() {
        return conditions;
    }

    public ChainedCondition setConditions(List<? extends BaseCondition> conditions) {
        this.conditions = conditions;
        return this;
    }

    @Override
    public ChainedCondition setType(Type type) {
        super.setType(type);
        return this;
    }

    @Override
    public ChainedCondition setOperator(Operator.Type operator) {
        super.setOperator(operator);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ChainedCondition that = (ChainedCondition) o;
        return Objects.equals(conditions, that.conditions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), conditions);
    }

    @Override
    public String toString() {
        return "CHAINEDCONDITION{\n" +
                super.toString() +
                "conditions=" + conditions + "\n" +
                "}";
    }
}
