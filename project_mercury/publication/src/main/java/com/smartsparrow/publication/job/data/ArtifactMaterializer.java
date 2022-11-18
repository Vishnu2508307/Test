package com.smartsparrow.publication.job.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.publication.job.enums.ArtifactType;

import javax.inject.Inject;
import java.util.UUID;

import static com.smartsparrow.dse.api.ResultSets.getNullableEnum;

class ArtifactMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ArtifactMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findById(final UUID jobId) {

        // @formatter:off
        final String QUERY = "SELECT" +
                "  id" +
                ", job_id" +
                ", artifact_type" +
                " FROM publication.artifact" +
                " WHERE job_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(jobId);
        return stmt;
    }

    public Artifact fromRow(Row row) {
        return new Artifact()
                .setId(row.getUUID("id"))
                .setJobId(row.getUUID("job_id"))
                .setArtifactType(getNullableEnum(row, "artifact_type", ArtifactType.class));
    }
}
