package com.smartsparrow.courseware.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

import java.util.UUID;


public class CoursewareChangeLogElementMutator extends SimpleTableMutator<ChangeLogByElement> {

    @Override
    public Statement upsert(ChangeLogByElement mutation) {
        // @formatter:off
        String QUERY =  "INSERT INTO courseware.changelog_by_element (" +
                "  element_id" +
                ", id" +
                ", account_id" +
                ", courseware_action" +
                ", on_element_id" +
                ", on_element_type" +
                ", on_parent_walkable_id" +
                ", on_parent_walkable_type" +
                ", on_element_title" +
                ", on_parent_walkable_title"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        bindUpsert(stmt,mutation);
        stmt.setIdempotent(true);
        return stmt;
    }

    @SuppressWarnings("Duplicates")
    public void bindUpsert(BoundStatement stmt, ChangeLogByElement mutation) {
        stmt.setUUID(0, mutation.getElementId());
        stmt.setUUID(1, mutation.getId());
        stmt.setUUID(2, mutation.getAccountId());
        stmt.setString(3, mutation.getCoursewareAction().name());
        stmt.setUUID(4, mutation.getOnElementId());
        stmt.setString(5, mutation.getOnElementType().name());
        optionalBind(stmt, 6, mutation.getOnParentWalkableId(), UUID.class);
        if (mutation.getOnParentWalkableType() != null) {
            stmt.setString(7, mutation.getOnParentWalkableType().name());
        }
        optionalBind(stmt, 8, mutation.getOnElementTitle(), String.class);
        optionalBind(stmt, 9, mutation.getOnParentWalkableTitle(), String.class);
    }
}
