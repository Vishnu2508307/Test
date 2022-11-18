package com.smartsparrow.eval.resolver;

/**
 * Any object that requires to be resolved must implement a resolvable interface
 * @param <T> the expected type of the value
 */
public interface Resolvable<T> {

    /**
     * @return the resolved value
     */
    T getResolvedValue();

    /**
     * Set the resolved value
     *
     * @param object the value to be set
     * @return a resolvable
     */
    Resolvable setResolvedValue(T object);
}
