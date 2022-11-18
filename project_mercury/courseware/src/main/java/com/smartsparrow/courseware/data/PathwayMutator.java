package com.smartsparrow.courseware.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.courseware.pathway.Pathway;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

class PathwayMutator extends SimpleTableMutator<Pathway> {

    @Override
    public String getUpsertQuery(Pathway mutation) {
        // @formatter:off
        return "INSERT INTO courseware.pathway ("
                + "  id"
                + ", type"
                + ", preload_pathway"
                + ") VALUES ( ?, ?, ?)";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, Pathway mutation) {
        stmt.bind(mutation.getId(),
                  Enums.asString(mutation.getType()),
                  Enums.asString(mutation.getPreloadPathway()));
    }
}
