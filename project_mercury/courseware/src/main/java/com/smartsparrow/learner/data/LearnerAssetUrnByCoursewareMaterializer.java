package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

class LearnerAssetUrnByCoursewareMaterializer  implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public LearnerAssetUrnByCoursewareMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchAll(final UUID elementId, final UUID changeId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + " asset_urn"
                + " FROM learner.asset_urn_by_courseware"
                + " WHERE element_id = ?"
                + " AND change_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(elementId, changeId);
        return stmt;
    }

    public String fromRow(Row row) {
        return row.getString("asset_urn");
    }
}
