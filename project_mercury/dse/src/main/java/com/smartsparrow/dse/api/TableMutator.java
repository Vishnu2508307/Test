package com.smartsparrow.dse.api;

import com.datastax.driver.core.Statement;

/**
 * Interface that Table Mutators must implement
 * (i.e. the objects that build mutations/queries to tables)
 *
 */
public interface TableMutator<T> {

    /**
     * Create an upsert (insert/update) mutation statement.
     * 
     * @param mutation
     * @return
     */
    public Statement upsert(T mutation);

    /**
     * Create a delete mutation.
     *
     * @param mutation
     * @return
     */
    public Statement delete(T mutation);

    /**
     *
     */
    default boolean isForceLocalCL() {
        return "1".equals(System.getProperty("forceLocalCL", "0"));
    }

}
