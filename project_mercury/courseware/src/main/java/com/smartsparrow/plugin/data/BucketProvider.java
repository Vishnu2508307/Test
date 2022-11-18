package com.smartsparrow.plugin.data;

public interface BucketProvider<T, C> {
    /**
     * Get the object of type {@link C} given a parameterized type {@link T}
     *
     * @param t the parameterized type
     * @return the object of type {@link C}
     */
    C get(T t);
}
