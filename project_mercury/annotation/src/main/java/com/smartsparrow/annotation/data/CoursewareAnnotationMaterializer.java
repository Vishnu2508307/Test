package com.smartsparrow.annotation.data;

import java.io.IOException;
import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsparrow.annotation.service.CoursewareAnnotation;
import com.smartsparrow.annotation.service.Motivation;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.exception.IllegalStateFault;
import com.smartsparrow.util.Enums;

class CoursewareAnnotationMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    CoursewareAnnotationMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchLatestById(UUID annotationId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  id"
                + ", version"
                //+ ", annotation_type"
                + ", motivation"
                + ", creator_account_id"
                + ", body"
                + ", target"
                + ", root_element_id"
                + ", element_id"
                + ", resolved"
                + " FROM courseware.annotation"
                + " WHERE id=?"
                + " LIMIT 1";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(annotationId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public CoursewareAnnotation fromRow(Row row) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            //

            return new CoursewareAnnotation() //
                    .setId(row.getUUID("id")) //
                    .setVersion(row.getUUID("version")) //
                    // .setAnnotationType(row.getUUID("version")) [implemented as an interface default]
                    .setMotivation(Enums.of(Motivation.class, row.getString("motivation"))) //
                    .setCreatorAccountId(row.getUUID("creator_account_id")) //
                    .setBodyJson(row.isNull("body") ? null : mapper.readTree(row.getString("body"))) //
                    .setTargetJson(row.isNull("target") ? null : mapper.readTree(row.getString("target"))) //
                    .setRootElementId(row.getUUID("root_element_id")) //
                    .setElementId(row.getUUID("element_id"))
                    .setResolved(row.getBool("resolved"));
        } catch (IOException e) {
            // when parsing is invalid, it raises a JsonProcessingException
            throw new IllegalStateFault("error processing json");
        }
    }

}
