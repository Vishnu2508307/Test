package com.smartsparrow.asset.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class AssetByIconLibraryMutator extends SimpleTableMutator<IconsByLibrary> {

    @Override
    public String getUpsertQuery(IconsByLibrary mutation) {
        return "INSERT INTO asset.icons_by_library (" +
                "icon_library, " +
                "asset_urn) " +
                "VALUES (?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, IconsByLibrary mutation) {
        stmt.bind(
                mutation.getIconLibrary(),
                mutation.getAssetUrn()
        );
    }

    @Override
    public String getDeleteQuery(IconsByLibrary iconLibrary) {
        return "DELETE FROM asset.icons_by_library " +
                "WHERE icon_library = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, IconsByLibrary mutation) {
        stmt.bind(mutation.getIconLibrary());
    }
}
