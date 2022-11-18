package com.smartsparrow.cohort.data;

import static com.smartsparrow.dse.api.ResultSets.getNullableLong;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

public class CohortSummaryMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public CohortSummaryMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchById(UUID id) {
        String BY_ID = "SELECT id, " +
                "name, " +
                "type," +
                "start_date, " +
                "end_date, " +
                "finished_date, " +
                "workspace_id, " +
                "subscription_id, " +
                "creator_id FROM cohort.summary " +
                "WHERE id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_ID);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(id);
        stmt.setIdempotent(true);
        return stmt;
    }

    /**
     * Convert a row of data to a cohort summary.
     * `start_date`, `end_date` and `finished_date` will be mapped to null when the value is null in the table.
     *
     * @param row the {@link Row} to convert
     * @return a {@link CohortSummary}
     */
    public CohortSummary fromRow(Row row) {
        return new CohortSummary()
                .setId(row.getUUID("id"))
                .setName(row.getString("name"))
                .setType(Enums.of(EnrollmentType.class, row.getString("type")))
                .setStartDate(getNullableLong(row, "start_date"))
                .setEndDate(getNullableLong(row, "end_date"))
                .setFinishedDate(row.getUUID("finished_date"))
                .setWorkspaceId(row.getUUID("workspace_id"))
                .setCreatorId(row.getUUID("creator_id"))
                .setSubscriptionId(row.getUUID("subscription_id"));
    }
}
