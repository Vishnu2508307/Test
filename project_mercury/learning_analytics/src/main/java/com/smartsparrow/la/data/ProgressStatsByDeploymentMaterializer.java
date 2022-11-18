package com.smartsparrow.la.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class ProgressStatsByDeploymentMaterializer implements TableMaterializer {
    private final PreparedStatementCache preparedStatementCache;

    @Inject
    public ProgressStatsByDeploymentMaterializer(final PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    final static String QUERY = "SELECT " +
            " deployment_id" +
            ", courseware_element_id" +
            ", courseware_element_type" +
            ", stat_type" +
            ", stat_value" +
            " FROM learner_insight.progress_stats_by_deployment";

    public Statement findByDeploymentAndCoursewareElement(UUID deploymentId, UUID coursewareElementId) {
        //@formatter:off
        final String BY_DEPLOYMENT_COURSEWARE_ELEMENT= QUERY +
                " WHERE deployment_id= ?" +
                " AND courseware_element_id = ?";
        //@formatter:on
        BoundStatement stmt = preparedStatementCache.asBoundStatement(BY_DEPLOYMENT_COURSEWARE_ELEMENT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(deploymentId, coursewareElementId);
        return stmt;
    }

    public Statement findByDeploymentAndCoursewareElementAndStatType(UUID deploymentId,
                                                                     UUID coursewareElementId,
                                                                     StatType statType) {
        //@formatter:off
        final String BY_DEPLOYMENT_COURSEWARE_ELEMENT_STAT_TYPE = QUERY +
                " WHERE deployment_id= ?" +
                " AND courseware_element_id = ?" +
                " AND stat_type = ?";
        //@formatter:on
        BoundStatement stmt = preparedStatementCache.asBoundStatement(BY_DEPLOYMENT_COURSEWARE_ELEMENT_STAT_TYPE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(deploymentId, coursewareElementId, statType.name());
        return stmt;
    }

    public ProgressStatsByDeployment fromRow(final Row row) {
        return new ProgressStatsByDeployment()
                .setDeploymentId(row.getUUID("deployment_id"))
                .setCoursewareElementId(row.getUUID("courseware_element_id"))
                .setCoursewareElementType(Enum.valueOf(CoursewareElementType.class,
                                                       row.getString("courseware_element_type")))
                .setStatType(Enum.valueOf(StatType.class,
                                          row.getString("stat_type")))
                .setStatValue(row.getDouble("stat_value"));
    }
}
