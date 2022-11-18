package com.smartsparrow.cohort.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class InstanceByTemplateMutator extends SimpleTableMutator<UUID>  {

    public Statement upsert(UUID templateCohortId, UUID instanceCohortId) {
        String UPSERT = "INSERT INTO cohort.instance_by_template (" +
                "template_id, " +
                "instance_id) " +
                "VALUES (?,?)";

        BoundStatement stmt = stmtCache.asBoundStatement(UPSERT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(templateCohortId, instanceCohortId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement remove(UUID templateCohortId, UUID instanceCohortId) {
        String REMOVE = "DELETE FROM cohort.instance_by_template " +
                "WHERE template_id = ? " +
                "AND instance_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(REMOVE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(templateCohortId, instanceCohortId);
        stmt.setIdempotent(true);
        return stmt;
    }

}
