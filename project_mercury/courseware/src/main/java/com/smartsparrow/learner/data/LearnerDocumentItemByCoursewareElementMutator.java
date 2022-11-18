package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class LearnerDocumentItemByCoursewareElementMutator extends SimpleTableMutator<LearnerDocumentItemTag> {

    @Override
    public String getUpsertQuery(LearnerDocumentItemTag mutation) {
        return "INSERT INTO learner.document_item_by_courseware_element (" +
                " element_id," +
                " deployment_id," +
                " change_id," +
                " document_item_id," +
                " document_id," +
                " element_type) VALUES (?,?,?,?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, LearnerDocumentItemTag mutation) {
        stmt.bind(
                mutation.getElementId(),
                mutation.getDeploymentId(),
                mutation.getChangeId(),
                mutation.getDocumentItemId(),
                mutation.getDocumentId(),
                mutation.getElementType().name()
        );
    }
}
