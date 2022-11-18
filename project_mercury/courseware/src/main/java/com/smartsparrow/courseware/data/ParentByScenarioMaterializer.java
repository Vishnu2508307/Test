package com.smartsparrow.courseware.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

public class ParentByScenarioMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ParentByScenarioMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchParent(UUID scenarioId) {
        final String SELECT = "SELECT " +
                "parent_id, " +
                "scenario_id, " +
                "parent_type " +
                "FROM courseware.parent_by_scenario " +
                "WHERE scenario_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(scenarioId);
        return stmt;
    }

    public ParentByScenario fromRow(Row row) {
        return new ParentByScenario()
                .setParentId(row.getUUID("parent_id"))
                .setScenarioId(row.getUUID("scenario_id"))
                .setParentType(Enums.of(CoursewareElementType.class, row.getString("parent_type")));
    }
}
