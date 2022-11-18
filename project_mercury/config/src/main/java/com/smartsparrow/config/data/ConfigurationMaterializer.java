package com.smartsparrow.config.data;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class ConfigurationMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ConfigurationMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    // @formatter:off
    private static final String FETCH_BY_REGION = "SELECT" +
                                                    " env_region" +
                                                    ", key" +
                                                    ", value" +
                                                    " FROM config.env" +
                                                    " WHERE env_region=?";

    private static final String FETCH_BY_REGION_AND_KEY = FETCH_BY_REGION + " and key=?";
    // @formatter:on

    public Statement fetchByKeyAndRegion(String key, String region) {
        BoundStatement stmt = stmtCache.asBoundStatement(FETCH_BY_REGION_AND_KEY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(region, key);
        stmt.setIdempotent(true);
        return stmt;
    }

    @SuppressWarnings("Duplicates")
    public Statement fetchByRegion(String region) {
        BoundStatement stmt = stmtCache.asBoundStatement(FETCH_BY_REGION);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(region);
        stmt.setIdempotent(true);
        return stmt;
    }
}
