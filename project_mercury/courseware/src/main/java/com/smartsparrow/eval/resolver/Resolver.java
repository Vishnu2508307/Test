package com.smartsparrow.eval.resolver;

import com.smartsparrow.eval.parser.Operand;

import reactor.core.publisher.Mono;

/**
 * The Resolver interface. A resolver must implement the behaviour for resolving a value at runtime.
 *
 * @param <R> a type that extends {@link Resolvable}. Represents the object the value should be resolved for
 * @param <T> a type that extends {@link Enum}. Represents the data type the resolved value should be cast to
 * @param <C> an object that represent the context in which the value should be resolved; providing the additional
 *           necessary information for the value to be resolved
 */
public interface Resolver<R extends Resolvable, T extends Enum, C> {

    enum Type {
        LITERAL,
        SCOPE,
        WEB
    }

    /**
     * Resolve the value for the {@link Operand} argument. The resolver context can be accessed via the
     * {@link Operand#getResolver()} method to perform more complicated logic.
     * This method is expected to return a new operand with the {@link Operand#getResolvedValue()} field defined
     *
     * @param resolvable the resolvable object to resolve the value for
     * @param context  the context for which the resolvable should be resolved
     * @return a mono of operand object with a resolved value defined
     */
    Mono<R> resolve(R resolvable, C context);

    /**
     * Resolve the value for the {@link Operand} argument. The resolver context can be accessed via the
     * {@link Operand#getResolver()} method to perform more complicated logic.
     * This method is expected to return a new operand with the {@link Operand#getResolvedValue()} field defined
     *
     * @param resolvable the resolvable object to resolve the value for
     * @param dataType the dataType the to resolve the final value to. Must be an enum
     * @param context  the context for which the resolvable should be resolved
     * @return a mono of operand object with a resolved value defined
     */
    Mono<R> resolve(R resolvable, T dataType, C context);
}
