--
-- courseware.student_scope_registry
-- --------------------
-- Tracks registered plugin references to a student scope
--
-- Find all courseware elements that are registered to a student scope
-- > SELECT * FROM courseware.student_scope_registry WHERE student_scope_urn = ?
--
CREATE TABLE IF NOT EXISTS courseware.student_scope_registry (
    student_scope_urn timeuuid,
    element_id timeuuid,
    element_type text,
    plugin_id timeuuid,
    plugin_version text,
    PRIMARY KEY (student_scope_urn, element_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
