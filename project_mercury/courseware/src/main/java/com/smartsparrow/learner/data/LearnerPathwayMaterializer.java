package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.courseware.pathway.LearnerPathway;
import com.smartsparrow.courseware.pathway.LearnerPathwayBuilder;
import com.smartsparrow.courseware.pathway.PathwayType;
import com.smartsparrow.courseware.pathway.PreloadPathway;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

public class LearnerPathwayMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;
    private final Provider<LearnerPathwayBuilder> learnerPathwayBuilderProvider;

    @Inject
    public LearnerPathwayMaterializer(PreparedStatementCache stmtCache, Provider<LearnerPathwayBuilder> learnerPathwayBuilderProvider) {
        this.stmtCache = stmtCache;
        this.learnerPathwayBuilderProvider = learnerPathwayBuilderProvider;
    }

    public Statement findLatestDeployed(UUID id, UUID deploymentId) {
        final String SELECT = "SELECT " +
                "id, " +
                "deployment_id, " +
                "change_id, " +
                "config, " +
                "type, " +
                "preload_pathway " +
                "FROM learner.pathway " +
                "WHERE id = ? " +
                "AND deployment_id = ? " +
                "LIMIT 1";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(id, deploymentId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement findPathwaysByLatestDeployed(UUID id, UUID deploymentId) {
        final String SELECT = "SELECT " +
                "id, " +
                "deployment_id, " +
                "change_id, " +
                "config, " +
                "type, " +
                "preload_pathway " +
                "FROM learner.pathway " +
                "WHERE id = ? " +
                "AND deployment_id = ? ";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(id, deploymentId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public LearnerPathway fromRow(Row row) {
        return learnerPathwayBuilderProvider.get() //
                .build(Enums.of(PathwayType.class, row.getString("type")),
                       row.getUUID("id"),
                       row.getUUID("deployment_id"),
                       row.getUUID("change_id"),
                       row.getString("config"),
                       row.getString("preload_pathway") != null ?
                               Enums.of(PreloadPathway.class, row.getString("preload_pathway")) :
                               PreloadPathway.NONE);
    }
}
