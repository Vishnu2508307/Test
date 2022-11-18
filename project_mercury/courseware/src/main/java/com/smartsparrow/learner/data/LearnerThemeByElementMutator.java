package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class LearnerThemeByElementMutator extends SimpleTableMutator<LearnerThemeByElement> {

    @Override
    public String getUpsertQuery(LearnerThemeByElement mutation) {
        // @formatter:off
        return "INSERT INTO learner.theme_by_element (" +
                "  element_id" +
                ", theme_id" +
                ", theme_name" +
                ") VALUES (?, ?, ?)";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, LearnerThemeByElement mutation) {
        stmt.setUUID(0, mutation.getElementId());
        stmt.setUUID(1, mutation.getThemeId());
        stmt.setString(2, mutation.getThemeName());
    }

}
