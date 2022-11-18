package com.smartsparrow.eval.action;

import com.smartsparrow.util.DataType;

/**
 * All {@link Action} realization should take in a parameterized type which is an extension of this class
 * @param <T> the type of the value the context should return
 */
public interface ActionContext<T> {

    /**
     * @return the value
     */
    T getValue();

    /**
     * @return the data type pf the context value
     */
    DataType getDataType();
}
