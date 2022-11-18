package com.smartsparrow.cache.service;

import java.io.Serializable;

/**
 * Auxiliary class to wrap values, giving the option to store result even when source
 * is empty
 */
public class CachedMonoWrapper<V> implements Serializable {

    private static final long serialVersionUID = 6659778646683744687L;
    private V value;
    private Boolean emptyValue;

    Boolean isEmptyValue() {
        return emptyValue;
    }

    CachedMonoWrapper<V> setEmptyValue(Boolean emptyValue) {
        this.emptyValue = emptyValue;
        return this;
    }

    V getValue() {
        return value;
    }

    CachedMonoWrapper<V> setValue(V value) {
        this.value = value;
        return this;
    }

}
