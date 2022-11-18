package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class LearnerElementMetaInformationMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public LearnerElementMetaInformationMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findMetaInformation(final UUID elementId, final UUID deploymentId, final UUID changeId, final String key) {
        final String SELECT = "SELECT" +
                " element_id," +
                ", deployment_id" +
                ", change_id" +
                ", key" +
                ", value" +
                " FROM learner.learner_element_meta_information" +
                " WHERE element_id = ?" +
                " AND deployment_id = ?" +
                " AND change_id = ?" +
                " AND key = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(elementId, deploymentId, changeId, key);
        stmt.setIdempotent(true);
        return stmt;
    }

    public LearnerElementMetaInformation fromRow(Row row) {
        return new LearnerElementMetaInformation()
                .setElementId(row.getUUID("element_id"))
                .setDeploymentId(row.getUUID("deployment_id"))
                .setChangeId(row.getUUID("change_id"))
                .setKey(row.getString("key"))
                .setValue(row.getString("value"));
    }
}
