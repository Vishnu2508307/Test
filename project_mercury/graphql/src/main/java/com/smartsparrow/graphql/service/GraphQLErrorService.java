package com.smartsparrow.graphql.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;
import com.smartsparrow.exception.ErrorResponseType;
import com.smartsparrow.exception.Fault;

import graphql.ExceptionWhileDataFetching;
import graphql.GraphQLError;

/**
 * Functionality to process errors generated by GraphQL; should be wired in by the real error handlers.
 *
 */
@Singleton
public class GraphQLErrorService {

    private final static Logger log = LoggerFactory.getLogger(GraphQLErrorService.class);

    /**
     * Process any incoming errors:
     * 1. Allow pass-through of GraphQL and Fault Exceptions
     * 2. Log & remove any unhandled exceptions (replaced with a generic message)
     *
     * @param errors the incoming error list to process
     * @return a list of errors scrubbed of irrelevant exceptions.
     */
    public List<GraphQLError> processErrors(final List<GraphQLError> errors) {
        if (errors == null || errors.isEmpty()) {
            // return what was given.
            return errors;
        }

        // perform some basic debug logging.
        if (log.isDebugEnabled()) {
            errors //
                    .forEach(error -> {
                        if (log.isDebugEnabled()) {
                            log.debug("GraphQLError: {}", error.getMessage());
                        }
                    });
        }

        List<GraphQLError> ret = new ArrayList<>();

        //
        // Log as errors the GraphQLErrors not being sent back to the client.
        //
        List<GraphQLError> graphQLErrors = logUnhandledErrors(errors);
        if (!graphQLErrors.isEmpty()) {
            // there were unhandled exceptions, add a generic error message for each raised error / path.
            graphQLErrors //
                    .forEach(error -> ret.add(new GraphQLErrorMessage("An unknown error occurred.", error)));
        }

        //
        // Build a list of errors that can be sent back to the client.
        //
        List<GraphQLError> propagableErrors = collectPropagableErrors(errors);
        ret.addAll(propagableErrors);

        //
        return ret;
    }

    /**
     * Build a list of errors that can be sent back to the client.
     *
     * @param errors the GraphQL generated errors
     * @return a new list
     */
    public List<GraphQLError> collectPropagableErrors(final Collection<GraphQLError> errors) {
        return errors.stream() //
                .filter(this::shouldPropagateError) //
                .map(this::sanitizeError) //
                .collect(Collectors.toList());
    }

    /**
     * Log the unhandled errors.
     *
     * @param errors the GraphQL generated errors
     * @return the errors which are have been logged
     */
    public List<GraphQLError> logUnhandledErrors(final Collection<GraphQLError> errors) {
        //
        List<GraphQLError> unhandledErrors = errors.stream() //
                .filter(error -> !shouldPropagateError(error)) //
                .collect(Collectors.toList());

        // error log them.
        unhandledErrors //
                .forEach(error -> {
                    // GraphQLError is not part of the Java Exception heirarchy...
                    if (error instanceof Throwable) {
                        // a general unhandled exception
                        log.error("[G001] Error while making GraphQL query", (Throwable) error);
                    } else if (error instanceof ExceptionWhileDataFetching) {
                        // error while fetching data, log the underlying exception.
                        log.error("[G002] Error while making GraphQL query {}", //
                                  error.getMessage(), ((ExceptionWhileDataFetching) error).getException());
                    } else {
                        // an error that is not throwable.
                        log.error("[G003] Error while making GraphQL query ({}): {}", //
                                  error.getClass().getSimpleName(), error.getMessage());
                    }
                });

        return unhandledErrors;
    }

    /**
     * Cleanup the error;
     *
     * @param error the generated error
     * @return a cleaner version
     */
    public GraphQLError sanitizeError(final GraphQLError error) {

        // unfortunately, the framework currently used renders the entire object (incl. stacktraces)
        // on the wire and is difficult to replace.
        // an example with too much information is an ExceptionWhileDataFetching exception.

        if (error instanceof ExceptionWhileDataFetching) {
            Throwable unwrapped = ((ExceptionWhileDataFetching) error).getException();
            if(unwrapped instanceof ErrorResponseType) {

              Map<String, Object> exts = new HashMap<>();
                exts.put("code", ((ErrorResponseType) unwrapped).getResponseStatusCode());
                exts.put("type", ((ErrorResponseType) unwrapped).getType());
                exts.putAll(((ErrorResponseType) unwrapped).getExtensions());

                // ExceptionWhileDataFetching only initializes the extensions field if original error is GraphQLError
                // and offers no setters, so we pry it open and shove a map in there
                if (error.getExtensions() == null) {
                    try {
                        FieldUtils.writeField(error, "extensions", new HashMap<String, Object>(), true);
                    } catch (IllegalAccessException | IllegalArgumentException ex) {
                        log.error("Unable to add extensions field to GraphQLError when handling: " +
                                ((ExceptionWhileDataFetching) error).getException().getMessage(),
                                ex);
                    }
                }
                try {
                    error.getExtensions().putAll(exts);
                } catch (NullPointerException npe) {
                    log.error("GraphQLError extensions field failed to be initialized, check previous logged exceptions for cause");
                }
            }

        }

        // So, stub in a delegate.
        return new GraphQLErrorMessage(error);
    }

    /**
     * Determine if an error should be propagated to the end-client. This is determined by
     * checking if the underlying exception is a GraphQL exception or a Fault.
     *
     * @param error the error to check
     * @return true if the error should be propagated to the end-client as an error.
     */
    private boolean shouldPropagateError(final GraphQLError error) {
        // Exceptions generated while building the response will end up here
        // wrapped within an ExceptionWhileDataFetching Exception.
        if (error instanceof ExceptionWhileDataFetching) {
            Throwable unwrapped = ((ExceptionWhileDataFetching) error).getException();
            // allow GraphQLErrors and Faults to propagate through.
            return (unwrapped instanceof GraphQLError) || (unwrapped instanceof Fault);
        }

        // Allow others which are not real unhandled exceptions.
        return !(error instanceof Throwable);
    }
}