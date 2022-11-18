package com.smartsparrow.courseware.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class PathwayConfigMutator extends SimpleTableMutator<PathwayConfig> {

    @Override
    public String getUpsertQuery(PathwayConfig mutation) {
        return "INSERT INTO courseware.pathway_config (" +
                " pathway_id" +
                ", id" +
                ", config" +
                ") VALUES (?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, PathwayConfig mutation) {
        stmt.bind(
                mutation.getPathwayId(),
                mutation.getId(),
                mutation.getConfig()
        );
    }
}
