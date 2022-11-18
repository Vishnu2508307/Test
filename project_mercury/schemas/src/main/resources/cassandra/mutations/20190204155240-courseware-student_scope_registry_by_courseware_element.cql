--
-- courseware.student_scope_registry_by_courseware_element
-- --------------------
-- Tracks all plugin references that are registered to a student scope
--
-- Write a description of the query that this table supports
-- > SELECT * FROM courseware.student_scope_registry_by_courseware_element WHERE student_scope_urn = ?
--
CREATE TABLE IF NOT EXISTS courseware.student_scope_registry_by_courseware_element (
    element_id timeuuid,
    student_scope_urn timeuuid,
    element_type text,
    plugin_id timeuuid,
    plugin_version text,
    PRIMARY KEY (element_id, student_scope_urn)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
