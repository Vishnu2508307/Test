package data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class PatchSummaryMutator extends SimpleTableMutator<PatchSummary> {

    @Override
    public Statement upsert(PatchSummary mutation) {

        // @formatter:off
        String QUERY = "INSERT INTO diffsync.patch_summary (" +
                "  id" +
                ", client_id" +
                ", entity_id" +
                ", entity_name" +
                ", patches" +
                ", n_version" +
                ", m_version"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?)";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        bindUpsert(stmt, mutation);
        stmt.setIdempotent(true);
        return stmt;
    }

    @SuppressWarnings("Duplicates")
    public void bindUpsert(BoundStatement stmt, PatchSummary mutation) {
        stmt.setUUID(0, mutation.getPatchId());
        stmt.setString(1, mutation.getClientId());
        stmt.setUUID(2, mutation.getEntityId());
        stmt.setString(3, mutation.getEntityName());
        stmt.setString(4, mutation.getPatches());
        stmt.setLong(5, mutation.getN().getValue());
        stmt.setLong(6, mutation.getM().getValue());
    }
}
