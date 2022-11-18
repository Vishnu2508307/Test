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

class ArtifactContentMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ArtifactContentMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findById(final UUID artifactId) {

        // @formatter:off
        final String QUERY = "SELECT" +
                "  id" +
                ", artifact_id" +
                " FROM publication.artifact_content" +
                " WHERE artifact_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(artifactId);
        return stmt;
    }

    public ArtifactContent fromRow(Row row) {
        return new ArtifactContent()
                .setId(row.getUUID("id"))
                .setArtifactId(row.getUUID("artifact_id"));
    }
}
