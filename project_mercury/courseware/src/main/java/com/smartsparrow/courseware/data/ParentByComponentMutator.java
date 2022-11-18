package com.smartsparrow.courseware.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ParentByComponentMutator extends SimpleTableMutator<ParentByComponent> {

    @Override
    public String getUpsertQuery(ParentByComponent mutation) {
        return "INSERT INTO courseware.parent_by_component (" +
                "component_id, " +
                "parent_id, " +
                "parent_type) " +
                "VALUES (?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, ParentByComponent mutation) {
        stmt.bind(mutation.getComponentId(), mutation.getParentId(), mutation.getParentType().name());
    }

    @Override
    public String getDeleteQuery(ParentByComponent mutation) {
        return "DELETE FROM courseware.parent_by_component " +
                "WHERE component_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, ParentByComponent mutation) {
        stmt.bind(mutation.getComponentId());
    }
}
