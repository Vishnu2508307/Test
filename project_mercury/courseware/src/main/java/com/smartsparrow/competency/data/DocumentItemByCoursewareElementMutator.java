package com.smartsparrow.competency.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class DocumentItemByCoursewareElementMutator extends SimpleTableMutator<DocumentItemTag> {

    @Override
    public String getUpsertQuery(DocumentItemTag mutation) {
        return "INSERT INTO competency.document_item_by_courseware_element (" +
                " element_id," +
                " document_item_id," +
                " document_id," +
                " element_type) VALUES (?,?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, DocumentItemTag mutation) {
        stmt.bind(
                mutation.getElementId(),
                mutation.getDocumentItemId(),
                mutation.getDocumentId(),
                mutation.getElementType().name()
        );
    }

    @Override
    public String getDeleteQuery(DocumentItemTag mutation) {
        return "DELETE FROM competency.document_item_by_courseware_element" +
                " WHERE element_id = ?" +
                " AND document_item_Id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, DocumentItemTag mutation) {
        stmt.bind(
                mutation.getElementId(),
                mutation.getDocumentItemId()
        );
    }
}
