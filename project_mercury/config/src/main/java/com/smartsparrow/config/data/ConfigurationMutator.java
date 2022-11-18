package com.smartsparrow.config.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ConfigurationMutator extends SimpleTableMutator<EnvConfiguration> {

    @Override
    public String getUpsertQuery(EnvConfiguration mutation) {
        // @formatter:off
        return "INSERT INTO config.env ("
                + "  env_region"
                + ", key"
                + ", value"
                + ") VALUES ( ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, EnvConfiguration mutation) {
        stmt.bind(mutation.getRegion(), mutation.getKey(), mutation.getValue());
    }
}
