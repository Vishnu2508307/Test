package com.smartsparrow.la.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ProgressStatsByDeploymentMutator extends SimpleTableMutator<ProgressStatsByDeployment> {

    @Override
    public String getUpsertQuery(final ProgressStatsByDeployment mutation) {
        return "INSERT INTO learner_insight.progress_stats_by_deployment (" +
                "deployment_id" +
                ", courseware_element_id" +
                ",courseware_element_type" +
                ",stat_type" +
                ",stat_value" +
                ") VALUES (?, ?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(final BoundStatement stmt, final ProgressStatsByDeployment mutation) {
        stmt.bind(mutation.getDeploymentId(),
                  mutation.getCoursewareElementId(),
                  mutation.getCoursewareElementType(),
                  mutation.getStatType().name(),
                  mutation.getStatValue());
    }
}
