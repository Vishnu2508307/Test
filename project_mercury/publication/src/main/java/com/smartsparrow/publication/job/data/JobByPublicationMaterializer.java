package com.smartsparrow.publication.job.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

import javax.inject.Inject;
import java.util.UUID;

import static com.smartsparrow.dse.api.ResultSets.getNullableEnum;

class JobByPublicationMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public JobByPublicationMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findById(final UUID publicationId) {

        // @formatter:off
        final String QUERY = "SELECT" +
                "  publication_id" +
                ", job_id" +
                " FROM publication.job_by_publication" +
                " WHERE publication_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(publicationId);
        return stmt;
    }

    public JobByPublication fromRow(Row row) {
        return new JobByPublication()
                .setPublicationId(row.getUUID("publication_id"))
                .setJobId(row.getUUID("job_id"));
    }
}
