package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.competency.data.ItemAssociation;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class LearnerDocumentItemAssociationByDocumentMutator extends SimpleTableMutator<ItemAssociation> {

    @Override
    public String getUpsertQuery(ItemAssociation mutation) {
        return "INSERT INTO learner.document_item_association_by_document (" +
                "document_id, " +
                "association_id) VALUES (?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, ItemAssociation mutation) {
        stmt.bind(mutation.getDocumentId(), mutation.getId());
    }
}
