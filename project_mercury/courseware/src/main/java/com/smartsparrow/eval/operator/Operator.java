package com.smartsparrow.eval.operator;

public interface Operator {

    enum Type {
        AND,
        OR,
        NOT,
        CONTAINS,
        DOES_NOT_CONTAIN,
        CONTAINS_ONE_OF,
        DOES_NOT_CONTAIN_ONE_OF,
        CONTAINS_ANY_OF,
        DOES_NOT_CONTAIN_ANY_OF,
        STARTS_WITH,
        ENDS_WITH,
        GE,
        LE,
        GT,
        LT,
        IS,
        IS_NOT,
        EQUALS,
        NOT_EQUALS,
        INCLUDES_ANY_OF,
        INCLUDES_ALL_OF,
        DOES_NOT_INCLUDE,
        DOES_NOT_INCLUDE_ANY_OF,
        DOES_NOT_INCLUDE_ALL_OF
    }
}
