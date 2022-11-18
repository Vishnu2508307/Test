package com.smartsparrow.dse.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;

/**
 * A utility class to facilitate usage of cassandra prepared statements.
 *
 */
public class PreparedStatementCache {

    private final Logger log = LoggerFactory.getLogger(PreparedStatementCache.class);

    // a lock.
    private Lock lock = new ReentrantLock();

    // Query string hashcode to prepared statement
    private Map<Integer, PreparedStatement> cache = new ConcurrentHashMap<>();

    // Reference to the session to build queries against.
    private final Session session;

    @Inject
    public PreparedStatementCache(Session session) {
        this.session = session;
    }

    /**
     * Fetch or prepare the given query.
     *
     * @param query
     * @return
     */
    public PreparedStatement resolve(String query) {

        // let's not keep re-calculating the hashCode.
        int hashCode = query.hashCode();

        // is it cached already?
        if (cache.containsKey(hashCode)) {
            PreparedStatement stmt = cache.get(hashCode);
            if (log.isDebugEnabled()) {
                log.debug("reuse previously prepared statement: hashCode={} statement.preparedId={}", hashCode,
                        stmt.getPreparedId());
            }
            return stmt;
        }

        // avoid simultaneous building of statements
        // Future improvement: the lock would be based on the hashCode to allow higher concurrency
        lock.lock();
        try {
            // double check in case was waiting on another and already built.
            if (cache.containsKey(hashCode)) {
                return cache.get(hashCode);
            }

            log.info("preparing query: {}", query);
            PreparedStatement statement = session.prepare(query);
            cache.put(hashCode, statement);
            return statement;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Fetch the prepared query as a bound statement.
     *
     * @param query
     * @return
     */
    public BoundStatement asBoundStatement(String query) {
        PreparedStatement preparedStatement = resolve(query);
        return new BoundStatement(preparedStatement);
    }
}
