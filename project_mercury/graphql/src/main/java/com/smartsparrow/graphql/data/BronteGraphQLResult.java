package com.smartsparrow.graphql.data;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;

import graphql.GraphQLError;

/*
Container for the graphQL execution result
 */
public class BronteGraphQLResult {

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<GraphQLError> errors;
    @JsonInclude
    private Object data;

    public List<GraphQLError> getErrors() {
        return errors;
    }

    public BronteGraphQLResult setErrors(final List<GraphQLError> errors) {
        this.errors = errors;
        return this;
    }

    public Object getData() {
        return data;
    }

    public BronteGraphQLResult setData(final Object data) {
        this.data = data;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BronteGraphQLResult that = (BronteGraphQLResult) o;
        return Objects.equals(errors, that.errors) && Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(errors, data);
    }

    @Override
    public String toString() {
        return "BronteGraphQLResult{" +
                "errors=" + errors +
                ", data=" + data +
                '}';
    }
}
