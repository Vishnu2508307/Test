package com.smartsparrow.learner.data;


import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

class LearnerThemeVariantMutator extends SimpleTableMutator<LearnerThemeVariant> {

    @Override
    public String getUpsertQuery(LearnerThemeVariant mutation) {
        // @formatter:off
        return "INSERT INTO learner.theme_variant (" +
                "  theme_id" +
                ", variant_id" +
                ", variant_name" +
                ", config" +
                ", state" +
                ") VALUES (?, ?, ?, ?, ?)";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, LearnerThemeVariant mutation) {
        stmt.setUUID(0, mutation.getThemeId());
        stmt.setUUID(1, mutation.getVariantId());
        stmt.setString(2, mutation.getVariantName());
        stmt.setString(3, mutation.getConfig());
        stmt.setString(4, Enums.asString(mutation.getState()));

    }
}
