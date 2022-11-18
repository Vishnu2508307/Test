package com.smartsparrow.graphql.service;

import static graphql.GraphQL.newGraphQL;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.iam.service.AuthenticationContext;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.ExecutionResultImpl;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.schema.GraphQLSchema;

@Singleton
public class GraphQLService {

    private final static Logger log = LoggerFactory.getLogger(GraphQLService.class);
    private final GraphQLSchema graphQLSchema;
    private final GraphQLErrorService graphQLErrorService;
    private final GraphQL graphQL;

    @Inject
    public GraphQLService(GraphQLSchema graphQLSchema,
                          GraphQLErrorService graphQLErrorService) {
        this.graphQLSchema = graphQLSchema;
        this.graphQLErrorService = graphQLErrorService;
        this.graphQL = newGraphQL(graphQLSchema)
                .build();
    }

    /**
     * This is query execution for RTM calls only. For REST see {@link com.smartsparrow.graphql.resource.GraphQLResource}
     */
    public ExecutionResult query(final String query,
                                 final Map<String, Object> parameters,
                                 final AuthenticationContext authenticationContext) {
        if (log.isDebugEnabled()) {
            log.debug("Executing GraphQL query {} with parameters {}", query, parameters);
        }

        Map<String, Object> variables = parameters == null ? new HashMap<>() : parameters;

        BronteGQLContext bronteGQLContext = new BronteGQLContext()
                .setAuthenticationContext(authenticationContext);

        // build the execution path
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                .query(query)
                .variables(variables)
                .context(bronteGQLContext)
                .build();

        // execute it
        final ExecutionResult executionResult = graphQL.executeAsync(executionInput).join();

        if (log.isDebugEnabled()) {
            if (executionResult.getData() == null) {
                log.debug("GraphQL query result: null");
            } else {
                log.debug("GraphQL query result: {}", executionResult.getData().toString());
            }
            log.debug("GraphQL query errors: {}", executionResult.getErrors());
        }

        return executionResult;
    }

    /**
     * Sanitize the execution result;
     * 1. Scrub out errors
     * 2. Log unhandled errors (those which are not Faults)
     *
     * @param executionResult the result to sanitize
     * @return the same or new ExecutionResult
     */
    public ExecutionResult sanitize(final ExecutionResult executionResult) {
        List<GraphQLError> errors = executionResult.getErrors();
        if (errors == null || errors.isEmpty()) {
            // no errors, so return the same.
            return executionResult;
        }

        //
        List<GraphQLError> sanitizedErrors = graphQLErrorService.processErrors(errors);

        // Build a new result, with the cleaned up errors.
        return new ExecutionResultImpl(executionResult.getData(), //
                                       sanitizedErrors, //
                                       executionResult.getExtensions());
    }

}
