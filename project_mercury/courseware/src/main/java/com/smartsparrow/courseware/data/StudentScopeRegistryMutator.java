package com.smartsparrow.courseware.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class StudentScopeRegistryMutator extends SimpleTableMutator<ScopeReference> {

    @Override
    public String getUpsertQuery(ScopeReference mutation) {
        return "INSERT INTO courseware.student_scope_registry (" +
                " student_scope_urn" +
                ", element_id" +
                ", element_type" +
                ", plugin_id" +
                ", plugin_version) VALUES ( ?, ?, ?, ?, ? )";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, ScopeReference mutation) {
        stmt.bind(
                mutation.getScopeURN(),
                mutation.getElementId(),
                mutation.getElementType().name(),
                mutation.getPluginId(),
                mutation.getPluginVersion()
        );
    }

    @Override
    public String getDeleteQuery(ScopeReference mutation) {
        return "DELETE FROM courseware.student_scope_registry" +
                " WHERE student_scope_urn = ?" +
                " AND element_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, ScopeReference mutation) {
        stmt.bind(mutation.getScopeURN(), mutation.getElementId());
    }
}
