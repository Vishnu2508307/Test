package com.smartsparrow.workspace.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

class IconLibraryByThemeMutator extends SimpleTableMutator<IconLibraryByTheme> {

    @Override
    public String getUpsertQuery(IconLibraryByTheme mutation) {
        // @formatter:off
        return "INSERT INTO workspace.icon_library_by_theme ("
                + "theme_id"
                + ", icon_library"
                + ", status"
                + ") VALUES ( ?, ?, ?)";
        // @formatter:on
    }

    @Override
    public String getDeleteQuery(IconLibraryByTheme mutation) {
        return "DELETE FROM workspace.icon_library_by_theme " +
                "WHERE theme_id = ?";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, IconLibraryByTheme mutation) {
        stmt.bind(mutation.getThemeId(), mutation.getIconLibrary(), Enums.asString(mutation.getStatus()));
    }

    @Override
    public void bindDelete(BoundStatement stmt, IconLibraryByTheme mutation) {
        stmt.bind(mutation.getThemeId());
    }
}
