--
-- learner.student_scope_registry
-- --------------------
-- Tracks registered plugin references to a student scope in a learner environment.
--
-- Find all registered elements to the student scope for a deployment
-- > SELECT * FROM learner.student_scope_registry WHERE student_scope_urn = ? AND deployment_id = ? AND change_id = ?
--
-- Find if the element has been registered to the student scope for a deployment
-- > SELECT * FROM learner.student_scope_registry WHERE student_scope_urn = ? AND deployment_id = ? AND change_id = ? AND element_id = ?
--
CREATE TABLE IF NOT EXISTS learner.student_scope_registry (
    student_scope_urn timeuuid,
    deployment_id timeuuid,
    change_id timeuuid,
    element_id timeuuid,
    element_type text,
    plugin_id timeuuid,
    plugin_version text,
    PRIMARY KEY (student_scope_urn, deployment_id, change_id, element_id)
) WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
