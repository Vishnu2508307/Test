package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.asset.data.DocumentSource;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class DocumentSourceByAssetMutator extends SimpleTableMutator<DocumentSource> {

    @Override
    public String getUpsertQuery(DocumentSource mutation) {
        return "INSERT INTO learner.document_source_by_asset (" +
                "asset_id, " +
                "url) " +
                "VALUES (?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, DocumentSource mutation) {
        stmt.bind(
                mutation.getAssetId(),
                mutation.getUrl()
        );
    }
}
