package com.smartsparrow.competency.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class CoursewareElementByDocumentMutator extends SimpleTableMutator<DocumentItemTag> {

    @Override
    public String getUpsertQuery(DocumentItemTag mutation) {
        return "INSERT INTO competency.courseware_element_by_document (" +
                " document_id," +
                " element_id," +
                " document_item_id," +
                " element_type) VALUES (?,?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, DocumentItemTag mutation) {
        stmt.bind(
                mutation.getDocumentId(),
                mutation.getElementId(),
                mutation.getDocumentItemId(),
                mutation.getElementType().name()
        );
    }

    @Override
    public String getDeleteQuery(DocumentItemTag mutation) {
        return "DELETE FROM competency.courseware_element_by_document" +
                " WHERE document_id = ?" +
                " AND element_id = ?" +
                " AND document_item_id =?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, DocumentItemTag mutation) {
        stmt.bind(
                mutation.getDocumentId(),
                mutation.getElementId(),
                mutation.getDocumentItemId()
        );
    }
}
