package com.smartsparrow.courseware.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class StudentScopeRegistryByCoursewareElementMutator extends SimpleTableMutator<ScopeReference> {

    @Override
    public String getUpsertQuery(ScopeReference mutation) {
        return "INSERT INTO courseware.student_scope_registry_by_courseware_element (" +
                " element_id" +
                ", student_scope_urn" +
                ", element_type" +
                ", plugin_id" +
                ", plugin_version) VALUES ( ?, ?, ?, ?, ? )";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, ScopeReference mutation) {
        stmt.bind(
                mutation.getElementId(),
                mutation.getScopeURN(),
                mutation.getElementType().name(),
                mutation.getPluginId(),
                mutation.getPluginVersion()
        );
    }

    @Override
    public String getDeleteQuery(ScopeReference mutation) {
        return "DELETE FROM courseware.student_scope_registry_by_courseware_element" +
                " WHERE element_id = ?" +
                " AND student_scope_urn = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, ScopeReference mutation) {
        stmt.bind(mutation.getElementId(), mutation.getScopeURN());
    }
}
