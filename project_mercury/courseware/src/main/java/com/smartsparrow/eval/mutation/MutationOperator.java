package com.smartsparrow.eval.mutation;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum MutationOperator {
    ADD,
    REMOVE,
    @JsonEnumDefaultValue
    SET
}
