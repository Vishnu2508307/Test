package com.smartsparrow.graphql.service;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import graphql.ErrorClassification;
import graphql.GraphQLError;
import graphql.GraphqlErrorHelper;
import graphql.language.SourceLocation;

/**
 * An GraphQL Error Message. Used to remove unhandled exceptions (that are wrapped within the source error) or
 * to convey a specific type of error.
 */
public class GraphQLErrorMessage implements GraphQLError {

    private String message;
    private List<SourceLocation> locations;
    private ErrorClassification errorType;
    private List<Object> path;
    private Map<String, Object> extensions;

    /**
     * Construct a new object, using the source message as the message.
     * @param source the delegate
     */
    public GraphQLErrorMessage(GraphQLError source) {
        this(source.getMessage(), source);
    }

    /**
     * Construct a new object, using the specified message and source.
     *
     * @param message the message to return
     * @param source the delegate
     */
    public GraphQLErrorMessage(String message, GraphQLError source) {
        this.message = message;
        this.locations = source.getLocations();
        this.errorType = source.getErrorType();
        if (source.getPath() != null) {
            this.path = ImmutableList.copyOf(source.getPath());
        }
        if (source.getExtensions() != null) {
            this.extensions = ImmutableMap.copyOf(source.getExtensions());
        }
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public List<SourceLocation> getLocations() {
        return locations;
    }

    @JsonIgnore // ignored as this field not part of the specification, output by default as part of the servlet library
    @Override
    public ErrorClassification getErrorType() {
        return errorType;
    }

    @Override
    public List<Object> getPath() {
        return path;
    }

    @Override
    public Map<String, Object> getExtensions() {
        return extensions;
    }

    @Override
    public Map<String, Object> toSpecification() {
        // Do not use the source object's .toSpecification() as this may return the inner exception, which we are
        // actively suppressing, ie. calling it will take a normal RuntimeException and expose it as part of the message.
        return GraphqlErrorHelper.toSpecification(this);
    }
}
