package com.smartsparrow.eval.action.unsupported;

import java.util.Objects;

import com.smartsparrow.eval.action.ActionContext;
import com.smartsparrow.util.DataType;

public class UnsupportedActionContext implements ActionContext<String> {

    private String value;

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public DataType getDataType() {
        return DataType.STRING;
    }

    public UnsupportedActionContext setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnsupportedActionContext that = (UnsupportedActionContext) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "UnsupportedActionContext{" +
                "value='" + value + '\'' +
                '}';
    }
}
