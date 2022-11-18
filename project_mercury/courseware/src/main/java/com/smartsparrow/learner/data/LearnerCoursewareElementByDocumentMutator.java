package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class LearnerCoursewareElementByDocumentMutator extends SimpleTableMutator<LearnerDocumentItemTag> {

    @Override
    public String getUpsertQuery(LearnerDocumentItemTag mutation) {
        return "INSERT INTO learner.courseware_element_by_document (" +
                " document_id," +
                " deployment_id," +
                " change_id," +
                " element_id," +
                " document_item_id," +
                " element_type) VALUES (?,?,?,?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, LearnerDocumentItemTag mutation) {
        stmt.bind(
                mutation.getDocumentId(),
                mutation.getDeploymentId(),
                mutation.getChangeId(),
                mutation.getElementId(),
                mutation.getDocumentItemId(),
                mutation.getElementType().name()
        );
    }
}
