package com.smartsparrow.annotation.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.annotation.service.CoursewareAnnotation;
import com.smartsparrow.annotation.service.CoursewareAnnotationKey;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

class CoursewareAnnotationMutator extends SimpleTableMutator<CoursewareAnnotation> {

    @Override
    public String getUpsertQuery(CoursewareAnnotation mutation) {
        // @formatter:off
        return "INSERT INTO courseware.annotation ("
                + "  id"
                + ", version"
                + ", annotation_type"
                + ", motivation"
                + ", creator_account_id"
                + ", body"
                + ", target"
                + ", root_element_id"
                + ", element_id"
                + ", resolved"
                + ") VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, CoursewareAnnotation mutation) {
        stmt.bind(mutation.getId(), //
                  mutation.getVersion(), //
                  Enums.asString(mutation.getType()), //
                  Enums.asString(mutation.getMotivation()), //
                  mutation.getCreatorAccountId(), //
                  mutation.getBody(), //
                  mutation.getTarget(), //
                  mutation.getRootElementId(), //
                  mutation.getElementId(),
                  mutation.getResolved());
    }

    @Override
    public String getDeleteQuery(CoursewareAnnotation mutation) {
        // @formatter:off
        return "DELETE FROM courseware.annotation"
                + " WHERE id = ?";
        // @formatter:on
    }

    @Override
    public void bindDelete(BoundStatement stmt, CoursewareAnnotation mutation) {
        stmt.bind(mutation.getId());
    }

    public Statement resolveComments(CoursewareAnnotationKey coursewareAnnotationKey, Boolean resolved) {
        final String RESOLVE_ANNOTATION = "UPDATE courseware.annotation " +
                "SET resolved = ? " +
                "WHERE id = ? " +
                "AND version = ? ";

        BoundStatement stmt = stmtCache.asBoundStatement(RESOLVE_ANNOTATION);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(resolved, coursewareAnnotationKey.getId(), coursewareAnnotationKey.getVersion());
        stmt.setIdempotent(false);
        return stmt;
    }
}
