package com.smartsparrow.eval.mutation;

/**
 * Interface to apply a mutation to the first argument with the second argument
 *
 * @param <V> the object to mutate
 * @param <T> the value to perform the mutation for
 */
public interface MutationOperation<V, T> {

    /**
     * Apply the second argument value to the first argument performing a mutation of the original object
     *
     * @param v the object to mutate
     * @param t the value to apply to the mutation
     * @return the mutated object
     */
    V apply(V v, T t);
}
