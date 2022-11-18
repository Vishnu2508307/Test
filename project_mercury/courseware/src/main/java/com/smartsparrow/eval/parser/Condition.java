package com.smartsparrow.eval.parser;

import com.smartsparrow.eval.operator.Operator;

public interface Condition {

    enum Type {
        CHAINED_CONDITION,
        EVALUATOR
    }

    Type getType();

    Operator.Type getOperator();
}
