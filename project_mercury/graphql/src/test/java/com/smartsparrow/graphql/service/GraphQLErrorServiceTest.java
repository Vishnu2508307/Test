package com.smartsparrow.graphql.service;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Maps.newHashMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;

import graphql.ErrorType;
import graphql.ExceptionWhileDataFetching;
import graphql.GraphQLError;
import graphql.language.SourceLocation;

class GraphQLErrorServiceTest {

    @InjectMocks
    private GraphQLErrorService graphQLErrorService;

    // Test exceptions.
    private ExceptionWhileDataFetching innerFault;
    private ExceptionWhileDataFetching innerGraphQLErrorException;
    private ExceptionWhileDataFetching innerUnhandledException;
    private GraphQLErrorException runtimeGraphQLError;
    private GraphQLError graphQLError;

    //
    static class GraphQLErrorException extends RuntimeException implements GraphQLError {
        @Override
        public List<SourceLocation> getLocations() {
            return null;
        }

        @Override
        public ErrorType getErrorType() {
            return null;
        }
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        //
        innerFault = mock(ExceptionWhileDataFetching.class);
        when(innerFault.getException()).thenReturn(new IllegalArgumentFault("fault"));

        innerGraphQLErrorException = mock(ExceptionWhileDataFetching.class);
        when(innerGraphQLErrorException.getException()).thenReturn(new GraphQLErrorException());

        innerUnhandledException = mock(ExceptionWhileDataFetching.class);
        when(innerUnhandledException.getException()).thenReturn(new RuntimeException("unhandled."));

        runtimeGraphQLError = new GraphQLErrorException();
        graphQLError = mock(GraphQLError.class);
    }

    //
    // collectPropagableErrors (also tested as part of processErrors)
    //

    @Test
    @DisplayName("collecting propagable errors are empty")
    void collectPropagableErrors_empty() {
        List<GraphQLError> errors = newArrayList();

        List<GraphQLError> actual = graphQLErrorService.collectPropagableErrors(errors);

        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("collecting propagable errors filters faults and graphql errors")
    void collectPropagableErrors_filters() {
        List<GraphQLError> errors = newArrayList(innerFault, // stays
                                                 innerGraphQLErrorException, // stays
                                                 innerUnhandledException, // removed
                                                 runtimeGraphQLError, // removed
                                                 graphQLError // stays
        );

        List<GraphQLError> actual = graphQLErrorService.collectPropagableErrors(errors);

        // actual errors are freshly generated, so this method one work, but here for expectations.
        // assertThat(actual).containsExactlyInAnyOrder(innerFault, innerGraphQLErrorException, graphQLError);
        assertThat(actual.size()).isEqualTo(3);
    }

    //
    // logUnhandledErrors (also tested as part of processErrors)
    //

    @Test
    @DisplayName("log unhandled errors errors for an empty list")
    void logUnhandledErrors_empty() {
        List<GraphQLError> errors = newArrayList();

        List<GraphQLError> actual = graphQLErrorService.logUnhandledErrors(errors);

        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("log unhandled errors errors filters")
    void logUnhandledErrors_filters() {
        List<GraphQLError> errors = newArrayList(innerFault, // removed
                                                 innerGraphQLErrorException, // removed
                                                 innerUnhandledException, // stays
                                                 runtimeGraphQLError, // stays
                                                 graphQLError // removed
        );

        List<GraphQLError> actual = graphQLErrorService.logUnhandledErrors(errors);

        // actual errors are freshly generated, so this method one work, but here for expectations.
        // assertThat(actual).containsExactlyInAnyOrder(innerUnhandledException, runtimeGraphQLError);
        assertThat(actual.size()).isEqualTo(2);
    }

    //
    // processErrors
    //

    @Test
    @DisplayName("process errors of an empty list should return same")
    void processErrors_empty() {
        List<GraphQLError> errors = new ArrayList<>();

        List<GraphQLError> actual = graphQLErrorService.processErrors(errors);

        assertThat(actual).isSameAs(errors);
    }

    @Test
    @DisplayName("process errors of an null list should return same")
    void processErrors_null() {
        List<GraphQLError> actual = graphQLErrorService.processErrors(null);

        assertThat(actual).isNull();
    }

    @Test
    @DisplayName("process errors returns the same amount of errors as given")
    void processErrors() {
        List<GraphQLError> errors = newArrayList(innerFault, // stays
                                                 innerGraphQLErrorException, // stays
                                                 innerUnhandledException, // stays
                                                 runtimeGraphQLError, // stays
                                                 graphQLError // stays
        );

        List<GraphQLError> actual = graphQLErrorService.processErrors(errors);

        assertThat(actual.size()).isEqualTo(5);
        // and that they are "different" / sanitized.
        assertThat(actual.get(0)).isNotIn(errors);
        assertThat(actual.get(1)).isNotIn(errors);
        assertThat(actual.get(2)).isNotIn(errors);
        assertThat(actual.get(3)).isNotIn(errors);
        assertThat(actual.get(4)).isNotIn(errors);
    }

    //
    // sanitizeError
    //

    @Test
    @DisplayName("sanitizeError return a new error with same values")
    void sanitizeError() {
        GraphQLError error = mock(GraphQLError.class);
        when(error.getMessage()).thenReturn("graphql error");
        List<SourceLocation> locations = newArrayList(new SourceLocation(1, 2, "three"));
        when(error.getLocations()).thenReturn(locations);
        ErrorType errorType = ErrorType.InvalidSyntax;
        when(error.getErrorType()).thenReturn(errorType);
        List<Object> path = newArrayList("a", "b", "c");
        when(error.getPath()).thenReturn(path);
        Map<String, Object> extensions = newHashMap("key", "value");
        when(error.getExtensions()).thenReturn(extensions);

        //
        GraphQLError actual = graphQLErrorService.sanitizeError(error);

        assertThat(actual).isNotSameAs(innerFault);
        assertThat(actual.getMessage()).isEqualTo("graphql error");
        assertThat(actual.getLocations()).isEqualTo(locations);
        assertThat(actual.getErrorType()).isEqualTo(errorType);
        assertThat(actual.getPath()).isEqualTo(path);
        assertThat(actual.getExtensions()).isEqualTo(extensions);
        // ensure toSpecification is not empty/null, expected looks like::
        // {message=graphql error, locations=[{line=1, column=2}], path=[a, b, c], extensions={key=value}}
        assertThat(actual.toSpecification()).isNotEmpty();
    }
}
