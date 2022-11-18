package com.smartsparrow.competency.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class CoursewareElementByDocumentItemMutator extends SimpleTableMutator<DocumentItemTag> {

    @Override
    public String getUpsertQuery(DocumentItemTag mutation) {
        return "INSERT INTO competency.courseware_element_by_document_item (" +
                " document_item_id," +
                " element_id," +
                " element_type," +
                " document_id) VALUES (?,?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, DocumentItemTag mutation) {
        stmt.bind(
                mutation.getDocumentItemId(),
                mutation.getElementId(),
                mutation.getElementType().name(),
                mutation.getDocumentId()
        );
    }

    @Override
    public String getDeleteQuery(DocumentItemTag mutation) {
        return "DELETE FROM competency.courseware_element_by_document_item" +
                " WHERE document_item_id = ?" +
                " AND element_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, DocumentItemTag mutation) {
        stmt.bind(
                mutation.getDocumentItemId(),
                mutation.getElementId()
        );
    }
}
