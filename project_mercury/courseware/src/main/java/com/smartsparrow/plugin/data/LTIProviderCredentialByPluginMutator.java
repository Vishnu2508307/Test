package com.smartsparrow.plugin.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class LTIProviderCredentialByPluginMutator extends SimpleTableMutator<LTIProviderCredential> {

    @Override
    public String getUpsertQuery(LTIProviderCredential mutation) {
        return "INSERT INTO plugin.lti_provider_cred_by_plugin ("
                + "id"
                + ", plugin_id"
                + ", lti_key"
                + ", lti_secret"
                + ", allowed_fields"
                + ") VALUES ( ?, ?, ?, ?, ? )";
    }

    @Override
    public String getDeleteQuery(LTIProviderCredential mutation) {
        return "DELETE FROM plugin.lti_provider_cred_by_plugin " +
                "WHERE lti_key = ? AND " +
                "plugin_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, LTIProviderCredential mutation) {
        stmt.bind(mutation.getKey(), mutation.getPluginId());
    }

    @Override
    public void bindUpsert(BoundStatement stmt, LTIProviderCredential mutation) {
        stmt.bind(mutation.getId(),
                  mutation.getPluginId(),
                  mutation.getKey(),
                  mutation.getSecret(),
                  mutation.getAllowedFields());
    }
}
