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

public class ScenarioByParentMaterializer implements TableMaterializer {

    private final PreparedStatementCache preparedStatementCache;

    @Inject
    public ScenarioByParentMaterializer(PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement findByParentLifecycle(UUID parentId, ScenarioLifecycle lifecycle) {
        final String QUERY = "SELECT " +
                "parent_id, " +
                "lifecycle, " +
                "scenario_ids, " +
                "parent_type " +
                "FROM courseware.scenario_by_parent " +
                "WHERE parent_id = ? " +
                "AND lifecycle = ?";
        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(parentId, lifecycle.name());
        return stmt;
    }

    public Statement findByParent(UUID parentId) {
        final String QUERY = "SELECT " +
                "parent_id, " +
                "lifecycle, " +
                "scenario_ids, " +
                "parent_type " +
                "FROM courseware.scenario_by_parent " +
                "WHERE parent_id = ? ";
        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(parentId);
        return stmt;
    }

    public ScenarioByParent fromRow(Row row) {
        return new ScenarioByParent()
                .setLifecycle(Enums.of(ScenarioLifecycle.class, row.getString("lifecycle")))
                .setParentId(row.getUUID("parent_id"))
                .setScenarioIds(row.getList("scenario_ids", UUID.class))
                .setParentType(Enums.of(CoursewareElementType.class, row.getString("parent_type")));
    }
}
