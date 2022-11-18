package com.smartsparrow.courseware.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

public class ManualGradingComponentByWalkableMutator extends SimpleTableMutator<ManualGradingComponentByWalkable> {

    @Override
    public String getUpsertQuery(ManualGradingComponentByWalkable mutation) {
        return "INSERT INTO courseware.manual_grading_component_by_walkable (" +
                " walkable_id" +
                ", component_id" +
                ", walkable_type" +
                ", component_parent_id" +
                ", component_parent_type" +
                ") VALUES (?, ?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, ManualGradingComponentByWalkable mutation) {
        stmt.bind(
                mutation.getWalkableId(),
                mutation.getComponentId(),
                Enums.asString(mutation.getWalkableType()),
                mutation.getComponentParentId(),
                Enums.asString(mutation.getParentComponentType())
        );
    }

    @Override
    public String getDeleteQuery(ManualGradingComponentByWalkable mutation) {
        return "DELETE FROM courseware.manual_grading_component_by_walkable" +
                " WHERE walkable_id = ?" +
                " AND component_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, ManualGradingComponentByWalkable mutation) {
        stmt.bind(
                mutation.getWalkableId(),
                mutation.getComponentId()
        );
    }
}
