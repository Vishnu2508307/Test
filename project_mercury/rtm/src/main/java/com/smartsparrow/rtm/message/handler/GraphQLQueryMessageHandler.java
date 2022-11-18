package com.smartsparrow.rtm.message.handler;

import javax.inject.Inject;
import javax.inject.Provider;

import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.graphql.service.GraphQLService;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.GraphQLQueryMessage;
import com.smartsparrow.rtm.message.send.LiteralBasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;

import graphql.ExecutionResult;

public class GraphQLQueryMessageHandler implements MessageHandler<GraphQLQueryMessage> {

    private final static Logger log = LoggerFactory.getLogger(GraphQLQueryMessageHandler.class);

    public static final String GRAPHQL_QUERY = "graphql.query";

    private final GraphQLService graphQLService;
    private final Provider<AuthenticationContext> authenticationContextProvider;

    @Inject
    public GraphQLQueryMessageHandler(GraphQLService graphQLService,
                                      final Provider<AuthenticationContext> authenticationContextProvider) {
        this.graphQLService = graphQLService;
        this.authenticationContextProvider = authenticationContextProvider;
    }

    @Override
    public void handle(Session session, GraphQLQueryMessage message) throws WriteResponseException {
        // execute the query.
        ExecutionResult executionResult = graphQLService.query(message.getQuery(),
                                                               message.getParameters(),
                                                               authenticationContextProvider.get());
        // sanitize the response (log errors, etc.)
        ExecutionResult sanitizedResult = graphQLService.sanitize(executionResult);

        // per ~ spec, the return of this call is nearly always successful (i.e. no graphql.error)
        // as data may be partially returned.
        LiteralBasicResponseMessage response = new LiteralBasicResponseMessage("graphql.response", message.getId());
        response.addAllFields(sanitizedResult.toSpecification());

        Responses.write(session, response);
    }

}
