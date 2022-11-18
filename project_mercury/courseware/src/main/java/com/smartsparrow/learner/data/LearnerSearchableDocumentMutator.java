package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class LearnerSearchableDocumentMutator extends SimpleTableMutator<LearnerSearchableDocument> {
    @Override
    public String getUpsertQuery(LearnerSearchableDocument mutation) {
        return "INSERT INTO learner.searchable_document (" +
                "deployment_id, " +
                "element_id, " +
                "searchable_field_id, " +
                "change_id, " +
                "product_id, " +
                "cohort_id, " +
                "element_type, " +
                "element_path, " +
                "element_path_type, " +
                "content_type, " +
                "summary, " +
                "body, " +
                "source, " +
                "preview, " +
                "tag) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, LearnerSearchableDocument mutation) {
        stmt.bind(mutation.getDeploymentId(),
                mutation.getElementId(),
                mutation.getSearchableFieldId(),
                mutation.getChangeId(),
                mutation.getProductId(),
                mutation.getCohortId(),
                mutation.getElementType().name(),
                mutation.getMutatorElementPath(),
                mutation.getElementPathType(),
                mutation.getContentType(),
                mutation.getSummary(),
                mutation.getBody(),
                mutation.getSource(),
                mutation.getPreview(),
                mutation.getTag());
    }
}
