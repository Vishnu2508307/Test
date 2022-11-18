package com.smartsparrow.plugin.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

class LTIProviderCredentialByPluginMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    LTIProviderCredentialByPluginMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    /**
     * Find all the LTI Provider Credentials for a plugin.
     *
     * @param pluginId
     * @return
     */
    public Statement fetch(final UUID pluginId) {
        final String QUERY = "SELECT "
                + "  plugin_id"
                + ", id"
                + ", lti_key"
                + ", lti_secret"
                + ", allowed_fields"
                + " FROM plugin.lti_provider_cred_by_plugin"
                + " WHERE plugin_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(pluginId);
        stmt.setIdempotent(true);
        return stmt;
    }

    /**
     * Find the LTI Provider Credential for a plugin and specific LTI Provider Key
     *
     * @param pluginId
     * @param ltiProviderKey
     * @return
     */
    public Statement fetch(final UUID pluginId, final String ltiProviderKey) {
        final String QUERY = "SELECT "
                + "  plugin_id"
                + ", id"
                + ", lti_key"
                + ", lti_secret"
                + ", allowed_fields"
                + " FROM plugin.lti_provider_cred_by_plugin"
                + " WHERE plugin_id = ?"
                + "   AND lti_key = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(pluginId, ltiProviderKey);
        stmt.setIdempotent(true);
        return stmt;
    }

    public LTIProviderCredential fromRow(Row row) {
        return new LTIProviderCredential()
                .setPluginId(row.getUUID("plugin_id"))
                .setId(row.getUUID("id"))
                .setKey(row.getString("lti_key"))
                .setSecret(row.getString("lti_secret"))
                .setAllowedFields(row.getSet("allowed_fields", String.class));
    }
}
