package com.smartsparrow.learner.data;

import java.util.Objects;

public class EvaluationTestContext implements EvaluationContext {

    private final String data;

    public EvaluationTestContext(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    @Override
    public Type getType() {
        return Type.TEST;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EvaluationTestContext that = (EvaluationTestContext) o;
        return Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    @Override
    public String toString() {
        return "EvaluationTestContext{" +
                "data='" + data + '\'' +
                '}';
    }
}
