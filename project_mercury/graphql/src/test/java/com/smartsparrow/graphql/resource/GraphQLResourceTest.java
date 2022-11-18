package com.smartsparrow.graphql.resource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.apache.commons.httpclient.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.data.QueryParams;
import com.smartsparrow.graphql.wiring.GraphQLServletErrorHandler;
import com.smartsparrow.iam.service.MutableAuthenticationContext;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.schema.GraphQLSchema;

public class GraphQLResourceTest {

    @Spy
    @InjectMocks
    private GraphQLResource graphQLResource;
    @Mock
    private GraphQLServletErrorHandler graphQLServletErrorHandler;
    @Mock
    private GraphQL graphQL;
    @Mock
    private GraphQLSchema graphQLSchema;
    @Mock
    private ExecutionResult executionResult;
    @Mock
    GraphQLError graphQLError;
    @Mock
    ExecutionInput executionInput;
    @Mock
    HttpServletRequest httpServletRequest;
    @Mock
    BronteGQLContext bronteGQLContext;


    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(executionResult.getErrors()).thenReturn(List.of(graphQLError));
        when(httpServletRequest.getAttribute(any())).thenReturn(bronteGQLContext);
    }

    @Test
    void handle_null_queryParams() {
        Response response = graphQLResource.handle(null, httpServletRequest);
        assertAll(() -> {
            assertEquals(HttpStatus.SC_OK, response.getStatus());
            assertNull(response.getEntity());
        });
    }

    @Test
    void graphQLGet_emptyQuery() throws JsonProcessingException {
        doReturn(Response.ok().build()).when(graphQLResource).handle(any(),any());

        Response response = graphQLResource.graphQLGet("", null, null, httpServletRequest);

        assertAll(() -> {
            assertNotNull(response);
            assertEquals(HttpStatus.SC_OK, response.getStatus());
        });
    }

    @Test
    void graphQLGet_emptyOperationName() throws JsonProcessingException {

        String result = "{ \"data\": { \"result\": \"pong\" } }";
        doReturn(Response.ok(result).build()).when(graphQLResource).handle(any(),any());

        Response response = graphQLResource.graphQLGet("{query {ping {result}}}", null, null, httpServletRequest);

        assertAll(() -> {
            assertNotNull(response);
            assertEquals(HttpStatus.SC_OK, response.getStatus());
            assertEquals(result, response.getEntity());
        });
    }

    @Test
    void graphQLGet_emptyVariables() throws JsonProcessingException {

        String result = "{ \"data\": { \"result\": \"pong\" } }";
        doReturn(Response.ok(result).build()).when(graphQLResource).handle(any(),any());

        Response response = graphQLResource.graphQLGet("{query {ping {result}}}", "query", null, httpServletRequest);

        assertAll(() -> {
            assertNotNull(response);
            assertEquals(HttpStatus.SC_OK, response.getStatus());
            assertEquals(result, response.getEntity());
        });
    }

    @Test
    void graphQLGet_valid() throws JsonProcessingException {

        String result = "{ \"data\": { \"result\": \"pong\" } }";
        doReturn(Response.ok(result).build()).when(graphQLResource).handle(any(),any());

        Response response = graphQLResource.graphQLGet("{query {ping {result}}}", "query", "{ \"id\": \"123\" }", httpServletRequest);

        assertAll(() -> {
            assertNotNull(response);
            assertEquals(HttpStatus.SC_OK, response.getStatus());
            assertEquals(result, response.getEntity());
        });
    }

    @Test
    void graphQLHead_emptyQuery() {
        doReturn(Response.ok().build()).when(graphQLResource).handle(any(),any());

        QueryParams queryParams = new QueryParams()
                .setQuery("")
                .setVariables(null)
                .setOperationName(null);

        Response response = graphQLResource.graphQLHead(queryParams,httpServletRequest);

        assertAll(() -> {
            assertNotNull(response);
            assertEquals(HttpStatus.SC_OK, response.getStatus());
        });
    }

    @Test
    void graphQLHead_emptyOperationName() {

        QueryParams queryParams = new QueryParams()
                .setQuery("{query {ping {result}}}")
                .setVariables(null)
                .setOperationName(null);

        String result = "{ \"data\": { \"result\": \"pong\" } }";
        doReturn(Response.ok(result).build()).when(graphQLResource).handle(any(),any());

        Response response = graphQLResource.graphQLHead(queryParams,httpServletRequest);

        assertAll(() -> {
            assertNotNull(response);
            assertEquals(HttpStatus.SC_OK, response.getStatus());
            assertEquals(result, response.getEntity());
        });
    }

    @Test
    void graphQLHead_emptyVariables() {

        String result = "{ \"data\": { \"result\": \"pong\" } }";
        QueryParams queryParams = new QueryParams()
                .setQuery("{query {ping {result}}}")
                .setOperationName("query")
                .setVariables(null);

        doReturn(Response.ok(result).build()).when(graphQLResource).handle(any(),any());

        Response response = graphQLResource.graphQLHead(queryParams,httpServletRequest);

        assertAll(() -> {
            assertNotNull(response);
            assertEquals(HttpStatus.SC_OK, response.getStatus());
            assertEquals(result, response.getEntity());
        });
    }

    @Test
    void graphQLHead_valid() {

        String result = "{ \"data\": { \"result\": \"pong\" } }";
        QueryParams queryParams = new QueryParams()
                .setQuery("{query {ping {result}}}")
                .setOperationName("query")
                .setVariables(new HashMap<>());

        doReturn(Response.ok(result).build()).when(graphQLResource).handle(any(),any());

        Response response = graphQLResource.graphQLHead(queryParams,httpServletRequest);

        assertAll(() -> {
            assertNotNull(response);
            assertEquals(HttpStatus.SC_OK, response.getStatus());
            assertEquals(result, response.getEntity());
        });
    }

    @Test
    void graphQLPost_emptyQuery() {
        doReturn(Response.ok().build()).when(graphQLResource).handle(any(),any());

        QueryParams queryParams = new QueryParams()
                .setQuery("")
                .setVariables(null)
                .setOperationName(null);

        Response response = graphQLResource.graphQLPost(queryParams,httpServletRequest);

        assertAll(() -> {
            assertNotNull(response);
            assertEquals(HttpStatus.SC_OK, response.getStatus());
        });
    }

    @Test
    void graphQLPost_emptyOperationName() {

        QueryParams queryParams = new QueryParams()
                .setQuery("{query {ping {result}}}")
                .setVariables(null)
                .setOperationName(null);

        String result = "{ \"data\": { \"result\": \"pong\" } }";
        doReturn(Response.ok(result).build()).when(graphQLResource).handle(any(),any());

        Response response = graphQLResource.graphQLPost(queryParams,httpServletRequest);

        assertAll(() -> {
            assertNotNull(response);
            assertEquals(HttpStatus.SC_OK, response.getStatus());
            assertEquals(result, response.getEntity());
        });
    }

    @Test
    void graphQLPost_emptyVariables() {

        String result = "{ \"data\": { \"result\": \"pong\" } }";
        QueryParams queryParams = new QueryParams()
                .setQuery("{query {ping {result}}}")
                .setOperationName("query")
                .setVariables(null);

        doReturn(Response.ok(result).build()).when(graphQLResource).handle(any(),any());

        Response response = graphQLResource.graphQLPost(queryParams,httpServletRequest);

        assertAll(() -> {
            assertNotNull(response);
            assertEquals(HttpStatus.SC_OK, response.getStatus());
            assertEquals(result, response.getEntity());
        });
    }

    @Test
    void graphQLPost_valid() {

        String result = "{ \"data\": { \"result\": \"pong\" } }";
        QueryParams queryParams = new QueryParams()
                .setQuery("{query {ping {result}}}")
                .setOperationName("query")
                .setVariables(new HashMap<>());

        doReturn(Response.ok(result).build()).when(graphQLResource).handle(any(),any());

        Response response = graphQLResource.graphQLHead(queryParams,httpServletRequest);

        assertAll(() -> {
            assertNotNull(response);
            assertEquals(HttpStatus.SC_OK, response.getStatus());
            assertEquals(result, response.getEntity());
        });
    }

    @Test
    void graphQLPut_emptyQuery() {
        doReturn(Response.ok().build()).when(graphQLResource).handle(any(),any());

        QueryParams queryParams = new QueryParams()
                .setQuery("")
                .setVariables(null)
                .setOperationName(null);

        Response response = graphQLResource.graphQLPut(queryParams,httpServletRequest);

        assertAll(() -> {
            assertNotNull(response);
            assertEquals(HttpStatus.SC_OK, response.getStatus());
        });
    }

    @Test
    void graphQLPut_emptyOperationName() {

        QueryParams queryParams = new QueryParams()
                .setQuery("{query {ping {result}}}")
                .setVariables(null)
                .setOperationName(null);

        String result = "{ \"data\": { \"result\": \"pong\" } }";
        doReturn(Response.ok(result).build()).when(graphQLResource).handle(any(),any());

        Response response = graphQLResource.graphQLPut(queryParams,httpServletRequest);

        assertAll(() -> {
            assertNotNull(response);
            assertEquals(HttpStatus.SC_OK, response.getStatus());
            assertEquals(result, response.getEntity());
        });
    }

    @Test
    void graphQLPut_emptyVariables() {

        String result = "{ \"data\": { \"result\": \"pong\" } }";
        QueryParams queryParams = new QueryParams()
                .setQuery("{query {ping {result}}}")
                .setOperationName("query")
                .setVariables(null);

        doReturn(Response.ok(result).build()).when(graphQLResource).handle(any(),any());

        Response response = graphQLResource.graphQLPut(queryParams, httpServletRequest);

        assertAll(() -> {
            assertNotNull(response);
            assertEquals(HttpStatus.SC_OK, response.getStatus());
            assertEquals(result, response.getEntity());
        });
    }

    @Test
    void graphQLPut_valid() {

        String result = "{ \"data\": { \"result\": \"pong\" } }";
        QueryParams queryParams = new QueryParams()
                .setQuery("{query {ping {result}}}")
                .setOperationName("query")
                .setVariables(new HashMap<>());

        doReturn(Response.ok(result).build()).when(graphQLResource).handle(any(),any());

        Response response = graphQLResource.graphQLPut(queryParams,httpServletRequest);

        assertAll(() -> {
            assertNotNull(response);
            assertEquals(HttpStatus.SC_OK, response.getStatus());
            assertEquals(result, response.getEntity());
        });
    }

    @Test
    void graphQLDelete_emptyQuery() {
        doReturn(Response.ok().build()).when(graphQLResource).handle(any(),any());

        QueryParams queryParams = new QueryParams()
                .setQuery("")
                .setVariables(null)
                .setOperationName(null);

        Response response = graphQLResource.graphQLDelete(queryParams,httpServletRequest);

        assertAll(() -> {
            assertNotNull(response);
            assertEquals(HttpStatus.SC_OK, response.getStatus());
        });
    }

    @Test
    void graphQLDelete_emptyOperationName() {

        QueryParams queryParams = new QueryParams()
                .setQuery("{query {ping {result}}}")
                .setVariables(null)
                .setOperationName(null);

        String result = "{ \"data\": { \"result\": \"pong\" } }";
        doReturn(Response.ok(result).build()).when(graphQLResource).handle(any(),any());

        Response response = graphQLResource.graphQLDelete(queryParams,httpServletRequest);

        assertAll(() -> {
            assertNotNull(response);
            assertEquals(HttpStatus.SC_OK, response.getStatus());
            assertEquals(result, response.getEntity());
        });
    }

    @Test
    void graphQLDelete_emptyVariables() {

        String result = "{ \"data\": { \"result\": \"pong\" } }";
        QueryParams queryParams = new QueryParams()
                .setQuery("{query {ping {result}}}")
                .setOperationName("query")
                .setVariables(null);

        doReturn(Response.ok(result).build()).when(graphQLResource).handle(any(),any());

        Response response = graphQLResource.graphQLDelete(queryParams,httpServletRequest);

        assertAll(() -> {
            assertNotNull(response);
            assertEquals(HttpStatus.SC_OK, response.getStatus());
            assertEquals(result, response.getEntity());
        });
    }

    @Test
    void graphQLDelete_valid() {

        String result = "{ \"data\": { \"result\": \"pong\" } }";
        QueryParams queryParams = new QueryParams()
                .setQuery("{query {ping {result}}}")
                .setOperationName("query")
                .setVariables(new HashMap<>());

        doReturn(Response.ok(result).build()).when(graphQLResource).handle(any(),any());

        Response response = graphQLResource.graphQLDelete(queryParams,httpServletRequest);

        assertAll(() -> {
            assertNotNull(response);
            assertEquals(HttpStatus.SC_OK, response.getStatus());
            assertEquals(result, response.getEntity());
        });
    }

    @Test
    void graphQLOptions_emptyQuery() {
        doReturn(Response.ok().build()).when(graphQLResource).handle(any(),any());

        QueryParams queryParams = new QueryParams()
                .setQuery("")
                .setVariables(null)
                .setOperationName(null);

        Response response = graphQLResource.graphQLOptions(queryParams,httpServletRequest);

        assertAll(() -> {
            assertNotNull(response);
            assertEquals(HttpStatus.SC_OK, response.getStatus());
        });
    }

    @Test
    void graphQLOptions_emptyOperationName() {

        QueryParams queryParams = new QueryParams()
                .setQuery("{query {ping {result}}}")
                .setVariables(null)
                .setOperationName(null);

        String result = "{ \"data\": { \"result\": \"pong\" } }";
        doReturn(Response.ok(result).build()).when(graphQLResource).handle(any(),any());

        Response response = graphQLResource.graphQLOptions(queryParams,httpServletRequest);

        assertAll(() -> {
            assertNotNull(response);
            assertEquals(HttpStatus.SC_OK, response.getStatus());
            assertEquals(result, response.getEntity());
        });
    }

    @Test
    void graphQLOptions_emptyVariables() {

        String result = "{ \"data\": { \"result\": \"pong\" } }";
        QueryParams queryParams = new QueryParams()
                .setQuery("{query {ping {result}}}")
                .setOperationName("query")
                .setVariables(null);

        doReturn(Response.ok(result).build()).when(graphQLResource).handle(any(),any());

        Response response = graphQLResource.graphQLOptions(queryParams,httpServletRequest);

        assertAll(() -> {
            assertNotNull(response);
            assertEquals(HttpStatus.SC_OK, response.getStatus());
            assertEquals(result, response.getEntity());
        });
    }

    @Test
    void graphQLOptions_valid() {

        String result = "{ \"data\": { \"result\": \"pong\" } }";
        QueryParams queryParams = new QueryParams()
                .setQuery("{query {ping {result}}}")
                .setOperationName("query")
                .setVariables(new HashMap<>());

        doReturn(Response.ok(result).build()).when(graphQLResource).handle(any(),any());

        Response response = graphQLResource.graphQLOptions(queryParams,httpServletRequest);

        assertAll(() -> {
            assertNotNull(response);
            assertEquals(HttpStatus.SC_OK, response.getStatus());
            assertEquals(result, response.getEntity());
        });
    }
}
