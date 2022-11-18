package com.smartsparrow.annotation.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Statement;
import com.smartsparrow.annotation.service.CoursewareAnnotationReadByUser;
import com.smartsparrow.annotation.service.Motivation;
import com.smartsparrow.dse.api.SimpleTableMutator;

class CoursewareAnnotationReadByUserMutator extends SimpleTableMutator<CoursewareAnnotationReadByUser> {

    @Override
    public String getUpsertQuery(CoursewareAnnotationReadByUser mutation) {
        // @formatter:off
        return "INSERT INTO courseware.annotation_read_by_user ("
                + "  root_element_id"
                + ", element_id"
                + ", annotation_id"
                + ", user_id"
                + ") VALUES ( ?, ? , ?, ?)";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, CoursewareAnnotationReadByUser mutation) {
        stmt.bind(mutation.getRootElementId(),
                  mutation.getElementId(),
                  mutation.getAnnotationId(),
                  mutation.getUserId());
    }

    @Override
    public String getDeleteQuery(CoursewareAnnotationReadByUser mutation) {
        // @formatter:off
        return "DELETE FROM courseware.annotation_read_by_user"
                + " WHERE root_element_id = ?"
                + " AND element_id = ?"
                + " AND annotation_id = ?"
                + " AND user_id = ?";
        // @formatter:on
    }

    @Override
    public void bindDelete(BoundStatement stmt, CoursewareAnnotationReadByUser mutation) {
        stmt.bind(mutation.getRootElementId(),
                  mutation.getElementId(),
                  mutation.getAnnotationId(), //
                  mutation.getUserId());
    }

    public Statement deleteAnnotation(final UUID rootElementId, final UUID elementId, final UUID annotationId) {
        String deleteQuery = "DELETE FROM courseware.annotation_read_by_user"
                + " WHERE root_element_id = ?"
                + " AND element_id = ?"
                + " AND annotation_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(deleteQuery);
        stmt.setConsistencyLevel(deleteConsistencyLevel());
        stmt.setIdempotent(isDeleteIdempotent());
        stmt.bind(rootElementId, elementId, annotationId);
        return stmt;
    }
}
