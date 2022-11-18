package com.smartsparrow.annotation.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Statement;
import com.smartsparrow.annotation.service.CoursewareAnnotation;
import com.smartsparrow.annotation.service.Motivation;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

import java.util.UUID;

class CoursewareAnnotationByMotivationMutator extends SimpleTableMutator<CoursewareAnnotation> {

    @Override
    public String getUpsertQuery(CoursewareAnnotation mutation) {
        // @formatter:off
        return "INSERT INTO courseware.annotation_by_motivation ("
                + "  root_element_id"
                + ", element_id"
                + ", motivation"
                + ", annotation_id"
                + ") VALUES ( ?, ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, CoursewareAnnotation mutation) {
        stmt.bind(mutation.getRootElementId(), //
                mutation.getElementId(), //
                Enums.asString(mutation.getMotivation()), //
                  mutation.getId());
    }

    @Override
    public String getDeleteQuery(CoursewareAnnotation mutation) {
        // @formatter:off
        return "DELETE FROM courseware.annotation_by_motivation"
                + " WHERE root_element_id = ?"
                + "   AND element_id = ?"
                + "   AND motivation = ?"
                + "   AND annotation_id = ?";
        // @formatter:on
    }

    @Override
    public void bindDelete(BoundStatement stmt, CoursewareAnnotation mutation) {
        stmt.bind(mutation.getRootElementId(), //
                  mutation.getElementId(), //
                  Enums.asString(mutation.getMotivation()), //
                  mutation.getId());
    }

    public Statement deleteAnnotations(final UUID elementId, final UUID rootElementId, final Motivation motivation) {
        String deleteQuery = "DELETE FROM courseware.annotation_by_motivation" +
                " WHERE element_id = ?" +
                " AND root_element_id = ?" +
                " And motivation = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(deleteQuery);
        stmt.setConsistencyLevel(deleteConsistencyLevel());
        stmt.setIdempotent(isDeleteIdempotent());
        stmt.bind(elementId, rootElementId, motivation.toString());
        return stmt;
    }
    public Statement deleteByRootElementId(UUID rootElementId) {
        String deleteQuery = "DELETE FROM courseware.annotation_by_motivation"
                + " WHERE root_element_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(deleteQuery);
        stmt.setConsistencyLevel(deleteConsistencyLevel());
        stmt.setIdempotent(isDeleteIdempotent());
        stmt.bind(rootElementId);
        return stmt;
    }
}
