package com.smartsparrow.courseware.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class ComponentConfigurationMutator extends SimpleTableMutator<ComponentConfig> {

    @Override
    public String getUpsertQuery(ComponentConfig mutation) {
        // @formatter:off
        return "INSERT INTO courseware.component_config ("
                + "  id"
                + ", component_id"
                + ", config"
                + ") VALUES ( ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, ComponentConfig mutation) {
        stmt.bind(mutation.getId(), mutation.getComponentId(), mutation.getConfig());
    }

}
