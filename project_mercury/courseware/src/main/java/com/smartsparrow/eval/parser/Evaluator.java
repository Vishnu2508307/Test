package com.smartsparrow.eval.parser;

import java.util.List;
import java.util.Objects;

import com.smartsparrow.eval.operator.Operator;
import com.smartsparrow.util.DataType;

public class Evaluator extends BaseCondition {

    private DataType operandType;
    private Operand lhs;
    private Operand rhs;
    private List<Option> options;

    public DataType getOperandType() {
        return operandType;
    }

    public Evaluator setOperandType(DataType operandType) {
        this.operandType = operandType;
        return this;
    }

    public Operand getLhs() {
        return lhs;
    }

    public Evaluator setLhs(Operand lhs) {
        this.lhs = lhs;
        return this;
    }

    public Operand getRhs() {
        return rhs;
    }

    public Evaluator setRhs(Operand rhs) {
        this.rhs = rhs;
        return this;
    }

    public List<Option> getOptions() {
        return options;
    }

    public Evaluator setOptions(List<Option> options) {
        this.options = options;
        return this;
    }

    @Override
    public Evaluator setType(Type type) {
        super.setType(type);
        return this;
    }

    @Override
    public Evaluator setOperator(Operator.Type operator) {
        super.setOperator(operator);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Evaluator evaluator = (Evaluator) o;
        return operandType == evaluator.operandType &&
                Objects.equals(lhs, evaluator.lhs) &&
                Objects.equals(rhs, evaluator.rhs) &&
                Objects.equals(options, evaluator.options);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), operandType, lhs, rhs, options);
    }

    @Override
    public String toString() {
        return "EVALUATOR{" +
                super.toString() +
                "operandType=" + operandType + "\n" +
                ", lhs=" + lhs + "\n" +
                ", rhs=" + rhs + "\n" +
                ", options=" + options + "\n" +
                '}';
    }
}
