package com.smartsparrow.data;

/**
 * Represents an object that can be consumed
 *
 * @param <T> the type of content the consumable holds
 */
public interface Consumable<T> {

    /**
     * @return the consumable content
     */
    T getContent();
}
