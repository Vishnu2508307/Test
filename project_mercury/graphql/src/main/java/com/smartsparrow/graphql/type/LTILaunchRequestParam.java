package com.smartsparrow.graphql.type;

import java.util.Objects;

import io.leangen.graphql.annotations.GraphQLInputField;

public class LTILaunchRequestParam {
    private String name;
    private String value;

    public LTILaunchRequestParam() {
    }

    public LTILaunchRequestParam(final String name, final String value) {
        this.name = name;
        this.value = value;
    }

    @GraphQLInputField(name = "name", description = "the name of the parameter")
    public String getName() {
        return name;
    }

    public LTILaunchRequestParam setName(final String name) {
        this.name = name;
        return this;
    }

    @GraphQLInputField(name = "value", description = "the value of the parameter")
    public String getValue() {
        return value;
    }

    public LTILaunchRequestParam setValue(final String value) {
        this.value = value;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LTILaunchRequestParam that = (LTILaunchRequestParam) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

    @Override
    public String toString() {
        return "LTILaunchRequestParam{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
