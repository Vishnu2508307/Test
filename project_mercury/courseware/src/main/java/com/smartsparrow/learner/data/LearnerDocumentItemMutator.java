package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class LearnerDocumentItemMutator extends SimpleTableMutator<LearnerDocumentItem> {

    @Override
    public String getUpsertQuery(LearnerDocumentItem mutation) {
        return "INSERT INTO learner.document_item (" +
                " id" +
                ", document_id" +
                ", full_statement" +
                ", abbreviated_statement" +
                ", human_coding_scheme" +
                ", created_by" +
                ", created_at" +
                ", modified_by" +
                ", modified_at) VALUES (?,?,?,?,?,?,?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, LearnerDocumentItem mutation) {
        stmt.bind(
                mutation.getId(),
                mutation.getDocumentId(),
                mutation.getFullStatement(),
                mutation.getAbbreviatedStatement(),
                mutation.getHumanCodingScheme(),
                mutation.getCreatedBy(),
                mutation.getCreatedAt(),
                mutation.getModifiedBy(),
                mutation.getModifiedAt()
        );
    }
}
