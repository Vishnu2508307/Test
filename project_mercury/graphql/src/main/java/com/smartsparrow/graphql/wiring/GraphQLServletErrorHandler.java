package com.smartsparrow.graphql.wiring;

import java.util.List;

import javax.inject.Inject;

import com.smartsparrow.graphql.service.GraphQLErrorService;

import graphql.GraphQLError;
import graphql.kickstart.execution.error.GraphQLErrorHandler;

/**
 * A handler of errors installed into the Servlet wiring.
 */
public class GraphQLServletErrorHandler implements GraphQLErrorHandler {

    private final GraphQLErrorService graphQLErrorService;

    @Inject
    public GraphQLServletErrorHandler(GraphQLErrorService graphQLErrorService) {
        this.graphQLErrorService = graphQLErrorService;
    }

    @Override
    public List<GraphQLError> processErrors(List<GraphQLError> errors) {
        return graphQLErrorService.processErrors(errors);
    }
}
