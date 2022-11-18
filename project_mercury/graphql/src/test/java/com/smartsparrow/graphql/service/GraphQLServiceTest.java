package com.smartsparrow.graphql.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import graphql.ExecutionResult;
import graphql.GraphQLError;
import graphql.schema.GraphQLSchema;

class GraphQLServiceTest {

    @Mock
    private GraphQLSchema graphQLSchema;

    @Mock
    private GraphQLErrorService graphQLErrorService;

    @InjectMocks
    private GraphQLService graphQLService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @DisplayName("sanitize returns same result when empty errors")
    void sanitize_noErrors_returnsSameEmpty() {
        ExecutionResult mockResult = mock(ExecutionResult.class);
        when(mockResult.getErrors()).thenReturn(Lists.emptyList());

        ExecutionResult actual = graphQLService.sanitize(mockResult);

        assertThat(actual).isSameAs(mockResult);
    }

    @Test
    @DisplayName("sanitize returns same result when null errors")
    void sanitize_noErrors_returnsSameNull() {
        ExecutionResult mockResult = mock(ExecutionResult.class);
        when(mockResult.getErrors()).thenReturn(null);

        ExecutionResult actual = graphQLService.sanitize(mockResult);

        assertThat(actual).isSameAs(mockResult);
    }

    @Test
    @DisplayName("sanitize returns new result with processed errors")
    void sanitize_errors_newResult() {
        List<GraphQLError> errors = Lists.newArrayList(mock(GraphQLError.class));
        ExecutionResult mockResult = mock(ExecutionResult.class);
        when(mockResult.getErrors()).thenReturn(errors);
        when(graphQLErrorService.processErrors(errors)).thenReturn(errors);

        ExecutionResult actual = graphQLService.sanitize(mockResult);

        assertThat(actual).isNotSameAs(mockResult);
        assertThat(actual.getErrors().size()).isEqualTo(1);
    }
}
