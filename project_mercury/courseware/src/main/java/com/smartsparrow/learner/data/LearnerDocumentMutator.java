package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class LearnerDocumentMutator extends SimpleTableMutator<LearnerDocument> {

    @Override
    public String getUpsertQuery(LearnerDocument mutation) {
        return "INSERT INTO learner.document (" +
                " id," +
                " title," +
                " created_at," +
                " created_by," +
                " modified_at," +
                " modified_by," +
                " document_version_id," +
                " origin) VALUES (?,?,?,?,?,?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, LearnerDocument mutation) {
        stmt.bind(
                mutation.getId(),
                mutation.getTitle(),
                mutation.getCreatedAt(),
                mutation.getCreatedBy(),
                mutation.getModifiedAt(),
                mutation.getModifiedBy(),
                mutation.getDocumentVersionId(),
                mutation.getOrigin()
        );
    }
}
