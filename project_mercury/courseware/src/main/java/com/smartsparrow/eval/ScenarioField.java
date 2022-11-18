package com.smartsparrow.eval;

public enum ScenarioField {

    TYPE("type"),
    CONDITIONS("conditions"),
    OPERATOR("operator"),
    OPERAND_TYPE("operandType"),
    LHS("lhs"),
    RHS("rhs"),
    OPTIONS("options"),
    VALUE("value"),
    RESOLVER("resolver");

    private final String label;

    ScenarioField(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
