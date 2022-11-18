package com.smartsparrow.graphql.resource;

import static com.google.common.base.Preconditions.checkArgument;
import static graphql.ExecutionInput.newExecutionInput;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.httpclient.HttpStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsparrow.exception.BadRequestException;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.data.BronteGraphQLResult;
import com.smartsparrow.graphql.data.QueryParams;
import com.smartsparrow.graphql.wiring.GraphQLServletErrorHandler;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.schema.GraphQLSchema;

@Path("")
public class GraphQLResource {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(GraphQLResource.class);

    private final GraphQL graphQL;
    private final GraphQLServletErrorHandler graphQLServletErrorHandler;
    private final ArrayList<String> cacheQuery = new ArrayList<>(Arrays.asList("defaultActivity",
                                                                               "activityById",
                                                                               "getElementTypeAndAncestry",
                                                                               "interactiveById",
                                                                               "getElementType",
                                                                               "staticDataElementId",
                                                                               "staticDataDefault"));

    @Inject
    public GraphQLResource(final GraphQLSchema schema,
                           final GraphQLServletErrorHandler graphQLServletErrorHandler) {
        this.graphQL = GraphQL
                .newGraphQL(schema)
                .build();
        this.graphQLServletErrorHandler = graphQLServletErrorHandler;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response graphQLGet(@QueryParam("query") String query,
                               @Nullable @QueryParam("operationName") String operationName,
                               @Nullable @QueryParam("variables") String variables,
                               @Context HttpServletRequest httpServletRequest) {

        QueryParams sanitizedQueryParams = parseAndValidateQueryParams(query, operationName, variables);
        return handle(sanitizedQueryParams, httpServletRequest);
    }

    @HEAD
    @Produces(MediaType.APPLICATION_JSON)
    public Response graphQLHead(QueryParams queryParams, @Context HttpServletRequest httpServletRequest) {
        checkArgument(queryParams != null, "QueryParams is required");
        QueryParams sanitizedQueryParams = parseAndValidateQueryParams(queryParams);
        return handle(sanitizedQueryParams, httpServletRequest);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response graphQLPost(QueryParams queryParams, @Context HttpServletRequest httpServletRequest) {
        checkArgument(queryParams != null, "QueryParams is required");
        QueryParams sanitizedQueryParams = parseAndValidateQueryParams(queryParams);
        return handle(sanitizedQueryParams, httpServletRequest);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response graphQLPut(QueryParams queryParams, @Context HttpServletRequest httpServletRequest) {
        checkArgument(queryParams != null, "QueryParams is required");
        QueryParams sanitizedQueryParams = parseAndValidateQueryParams(queryParams);
        return handle(sanitizedQueryParams, httpServletRequest);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response graphQLDelete(QueryParams queryParams, @Context HttpServletRequest httpServletRequest) {
        checkArgument(queryParams != null, "QueryParams is required");
        QueryParams sanitizedQueryParams = parseAndValidateQueryParams(queryParams);
        return handle(sanitizedQueryParams, httpServletRequest);
    }

    @OPTIONS
    @Produces(MediaType.APPLICATION_JSON)
    public Response graphQLOptions(@Nullable QueryParams queryParams, @Context HttpServletRequest httpServletRequest) {
        QueryParams sanitizedQueryParams = queryParams != null ? parseAndValidateQueryParams(queryParams) : null;
        return handle(sanitizedQueryParams, httpServletRequest);
    }

    protected Response handle(QueryParams queryParams, HttpServletRequest httpServletRequest) {
        if (queryParams == null) {
            return Response.ok().build();
        }

        MutableAuthenticationContext mutableAuthenticationContext = (MutableAuthenticationContext) httpServletRequest.getAttribute(
                "mutableAuthenticationContext");

        BronteGQLContext bronteGQLContext = new BronteGQLContext()
                .setMutableAuthenticationContext(mutableAuthenticationContext)
                .setAuthenticationContext(mutableAuthenticationContext);

        Response.ResponseBuilder responseBuilder = Response.ok();
        // This is currently set to 2 weeks. We may want to increase this, but likely need a way to purge.
        if (cacheQuery.contains(queryParams.getOperationName())) {
             responseBuilder.header(HttpHeaders.CACHE_CONTROL, "max-age=1209600");
        } else {
            responseBuilder.header(HttpHeaders.CACHE_CONTROL, "no-store");
        }
        responseBuilder.header("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");

        ExecutionInput.Builder executionInput = newExecutionInput()
                .context(bronteGQLContext)
                .query(queryParams.getQuery())
                .operationName(queryParams.getOperationName())
                .variables(queryParams.getVariables());

        CompletableFuture<ExecutionResult> promise = graphQL.executeAsync(executionInput);
        responseBuilder.header(HttpHeaders.CONTENT_TYPE, "application/json");
        responseBuilder.status(HttpStatus.SC_OK);

        CompletableFuture<BronteGraphQLResult> future = promise.handle((executionResult, t) -> {
            List<GraphQLError> graphQLErrors = executionResult.getErrors() != null &&
                    !executionResult.getErrors().isEmpty() ?
                    graphQLServletErrorHandler.processErrors(executionResult.getErrors()) : null;

            return new BronteGraphQLResult()
                    .setData(executionResult.getData())
                    .setErrors(graphQLErrors);
        });

        BronteGraphQLResult join = future.join();

        return responseBuilder.entity(join).build();
    }

    /**
     * Validate QueryParams{@link QueryParams}
     *
     * @param queryParams - the query parameters {@link QueryParams}
     * @return {@link QueryParams}
     */
    private QueryParams parseAndValidateQueryParams(QueryParams queryParams) {
        try {
            Map<String, Object> variables = queryParams.getVariables();
            String variablesJsonString = new ObjectMapper().writeValueAsString(variables);
            return parseAndValidateQueryParams(queryParams.getQuery(),
                                               queryParams.getOperationName(),
                                               variablesJsonString);
        } catch (JsonProcessingException e) {
            log.error("There was an exception processing the request {}", e.getMessage());
            throw new BadRequestException("Variables map is not a valid JSON object");
        }
    }

    /**
     * Validate properties of QueryParams
     *
     * @param query - the graphQL query string
     * @param operationName - the operation name if available
     * @param variables - the stringified variables map
     * @return {@link QueryParams}
     */
    private QueryParams parseAndValidateQueryParams(String query, String operationName, String variables) {
        try {
            TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>() {
            };

            Map<String, Object> variablesMap = variables != null ?
                    new ObjectMapper().readValue(variables, typeRef) :
                    new HashMap<>();

            return new QueryParams()
                    .setQuery(query)
                    .setOperationName(operationName)
                    .setVariables(variablesMap);

        } catch (IllegalArgumentException | JsonProcessingException e) {
            log.error("There was an exception processing the request {}", e.getMessage());
            throw new BadRequestException("Variables map is not a valid JSON object");
        }
    }
}
