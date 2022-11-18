package com.smartsparrow.graphql.data;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * GraphQL query parameters parsed into QueryParams value object
 */
public class QueryParams {

    private String query;
    private String operationName;
    private Map<String, Object> variables = Collections.emptyMap();

    public String getQuery() {
        return query;
    }

    public QueryParams setQuery(final String query) {
        this.query = query;
        return this;
    }

    public String getOperationName() {
        return operationName;
    }

    public QueryParams setOperationName(final String operationName) {
        this.operationName = operationName;
        return this;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public QueryParams setVariables(final Map<String, Object> variables) {
        this.variables = variables;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueryParams that = (QueryParams) o;
        return Objects.equals(query, that.query) && Objects.equals(operationName,
                                                                   that.operationName) && Objects.equals(
                variables,
                that.variables);
    }

    @Override
    public int hashCode() {
        return Objects.hash(query, operationName, variables);
    }

    @Override
    public String toString() {
        return "QueryParams{" +
                "query='" + query + '\'' +
                ", operationName='" + operationName + '\'' +
                ", variables=" + variables +
                '}';
    }
}
