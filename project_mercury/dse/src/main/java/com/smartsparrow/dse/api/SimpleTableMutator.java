package com.smartsparrow.dse.api;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.exceptions.InvalidTypeException;

/**
 * Create a Mutator that is "Simple". Extend this class and define the queries that the Mutator will support and
 * override any of the default implementations as needed.
 *
 * @param <T> the type of the Mutation object.
 */
public abstract class SimpleTableMutator<T> implements TableMutator<T> {

    private static final Logger log = LoggerFactory.getLogger(SimpleTableMutator.class);

    @Inject
    protected PreparedStatementCache stmtCache;

    /**
     * Default implementation for creation of an upsert (insert/update) mutation statement.
     *
     * @param mutation
     * @return
     */
    @Override
    public Statement upsert(T mutation) {
        String upsertQuery = getUpsertQuery(mutation);
        if (log.isDebugEnabled()) {
            log.debug("upsert: {}", upsertQuery);
        }

        BoundStatement stmt = stmtCache.asBoundStatement(upsertQuery);
        stmt.setConsistencyLevel(upsertConsistencyLevel());
        stmt.setIdempotent(isUpsertIdempotent());
        bindUpsert(stmt, mutation);

        return stmt;
    }

    /**
     * Default implementation for this class is not to support an upsert operation.
     *
     * @return the query string for an upsert query using '?' as a PreparedStatement placeholder
     * @param mutation
     */
    public String getUpsertQuery(T mutation) {
        throw new UnsupportedOperationException("upsert not supported");
    }

    /**
     * Perform the binding of the mutation to the upsert Statement.
     *
     * The default implementation performs no binding.
     *
     * @param stmt the statement to bind to
     * @param mutation the mutation use for binding
     */
    public void bindUpsert(BoundStatement stmt, T mutation) {
    }

    /**
     * Default implementation for the upsert query is {@code ConsistencyLevel.LOCAL_QUORUM}.
     *
     * Override this method if the Mutator uses a different consistency level.
     *
     * @return the consistency level to use, default: {@code ConsistencyLevel.LOCAL_QUORUM}
     */
    public ConsistencyLevel upsertConsistencyLevel() {
        return ConsistencyLevel.LOCAL_QUORUM;
    }

    /**
     * Default implementation for the upsert is that it is idempotent (can be retried safely without side effects)
     *
     * Override this method if the Mutator creates queries that are not idempotent.
     *
     * @return the upsert idempotent attribute, default: true
     */
    public boolean isUpsertIdempotent() {
        return true;
    }

    /**
     * Default implementation for creation of a delete mutation statement.
     *
     * @param mutation
     * @return
     */
    @Override
    public Statement delete(T mutation) {
        String deleteQuery = getDeleteQuery(mutation);
        if (log.isDebugEnabled()) {
            log.debug("delete: {}", deleteQuery);
        }
        BoundStatement stmt = stmtCache.asBoundStatement(deleteQuery);
        stmt.setConsistencyLevel(deleteConsistencyLevel());
        stmt.setIdempotent(isDeleteIdempotent());
        bindDelete(stmt, mutation);
        return stmt;
    }

    /**
     * Default implemenation for this class is not to support a delete operation.
     *
     * @return the query string for an upsert query using '?' as a PreparedStatement placeholder
     * @param mutation
     */
    public String getDeleteQuery(T mutation) {
        throw new UnsupportedOperationException("delete not supported");
    }

    /**
     * Perform the binding of the mutation to the delete Statement.
     *
     * The default implementation performs no binding.
     *
     * @param stmt the statement to bind to
     * @param mutation the mutation use for binding
     */
    public void bindDelete(BoundStatement stmt, T mutation) {
    }

    /**
     * Default implementation for the delete query is {@code ConsistencyLevel.LOCAL_QUORUM}.
     *
     * Override this method if the Mutator uses a different consistency level.
     *
     * @return the consistency level to use, default: {@code ConsistencyLevel.LOCAL_QUORUM}
     */
    public ConsistencyLevel deleteConsistencyLevel() {
        return ConsistencyLevel.LOCAL_QUORUM;
    }

    /**
     * Default implementation for the delete is that it is idempotent (can be retried safely without side effects)
     *
     * Override this method if the Mutator creates queries that are not idempotent.
     *
     * @return the upsert idempotent attribute, default: true
     */
    public boolean isDeleteIdempotent() {
        return true;
    }

    /**
     * Bind a non <code>null</code> value to the desired position in the statement
     *
     * @param stmt the bound statement to bind the value to
     * @param position the position the value should be bound at, 0 based.
     * @param v the value type
     * @param type the class type of the value (If a codec is not found for this type) the method fails
     * @throws InvalidTypeException when the codec is not found for the specified type
     */
    protected <V> void optionalBind(BoundStatement stmt, int position, V v, Class<V> type) {
        if (v != null) {
            stmt.set(position, v, type);
        }
    }

}
