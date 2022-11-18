package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class LearnerCoursewareElementByDocumentItemMutator extends SimpleTableMutator<LearnerDocumentItemTag> {

    @Override
    public String getUpsertQuery(LearnerDocumentItemTag mutation) {
        return "INSERT INTO learner.courseware_element_by_document_item (" +
                " document_item_id," +
                " deployment_id," +
                " change_id," +
                " element_id," +
                " document_id," +
                " element_type) VALUES(?,?,?,?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, LearnerDocumentItemTag mutation) {
        stmt.bind(
                mutation.getDocumentItemId(),
                mutation.getDeploymentId(),
                mutation.getChangeId(),
                mutation.getElementId(),
                mutation.getDocumentId(),
                mutation.getElementType().name()
        );
    }
}
