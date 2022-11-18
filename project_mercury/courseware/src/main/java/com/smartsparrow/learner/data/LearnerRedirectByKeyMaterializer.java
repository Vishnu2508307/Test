package com.smartsparrow.learner.data;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.learner.redirect.LearnerRedirectType;
import com.smartsparrow.util.Enums;

class LearnerRedirectByKeyMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public LearnerRedirectByKeyMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetch(final LearnerRedirectType type, final String key) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  id"
                + ", version"
                + ", redirect_type"
                + ", redirect_key"
                + ", destination_path"
                + " FROM learner_redirect.by_key"
                + " WHERE redirect_type = ?"
                + "   AND redirect_key = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(Enums.asString(type), key);
        return stmt;
    }

}
