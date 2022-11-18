package com.smartsparrow.courseware.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

import javax.inject.Inject;
import java.util.UUID;

import static com.smartsparrow.dse.api.ResultSets.getNullableEnum;

public class CoursewareChangeLogProjectMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public CoursewareChangeLogProjectMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findChangeLogByProject(final UUID projectId, int limit) {

        // @formatter:off
        final String QUERY = "SELECT" +
                " project_id" +
                ", id" +
                ", account_id" +
                ", courseware_action" +
                ", on_element_id" +
                ", on_element_type" +
                ", on_parent_walkable_id" +
                ", on_parent_walkable_type" +
                ", on_element_title" +
                ", on_parent_walkable_title" +
                " FROM courseware.changelog_by_project" +
                " WHERE project_id = ?" +
                " LIMIT "+ limit;
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(projectId);
        return stmt;
    }

    public ChangeLogByProject fromRow(Row row) {
        return new ChangeLogByProject()
                .setProjectId(row.getUUID("project_id"))
                .setId(row.getUUID("id"))
                .setAccountId(row.getUUID("account_id"))
                .setCoursewareAction(Enums.of(CoursewareAction.class, row.getString("courseware_action")))
                .setOnElementId(row.getUUID("on_element_id"))
                .setOnElementType(Enums.of(CoursewareElementType.class, row.getString("on_element_type")))
                .setOnParentWalkableId(row.getUUID("on_parent_walkable_id"))
                .setOnParentWalkableType(getNullableEnum(row, "on_parent_walkable_type", CoursewareElementType.class))
                .setOnElementTitle(row.getString("on_element_title"))
                .setOnParentWalkableTitle(row.getString("on_parent_walkable_title"));
    }
}
