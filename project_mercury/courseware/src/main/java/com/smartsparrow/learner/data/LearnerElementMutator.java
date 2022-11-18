package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

public class LearnerElementMutator extends SimpleTableMutator<LearnerCoursewareElement> {

    @Override
    public String getUpsertQuery(LearnerCoursewareElement mutation) {
        return "INSERT INTO learner.element (" +
                "id, " +
                "deployment_id, " +
                "change_id, " +
                "element_type) VALUES (?,?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, LearnerCoursewareElement mutation) {
        stmt.bind(
                mutation.getId(),
                mutation.getDeploymentId(),
                mutation.getChangeId(),
                Enums.asString(mutation.getElementType())
        );
    }
}
