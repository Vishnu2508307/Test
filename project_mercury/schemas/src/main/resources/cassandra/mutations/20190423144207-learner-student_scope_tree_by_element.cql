--
-- learner.student_scope_tree_by_element
-- --------------------
-- Track student scope for each leaf in the tree starting from a given element id.
--
-- Find all the student scope that have been initialised for any element in the sub-tree for a given courseware element
-- > SELECT * FROM learner.student_scope_tree_by_element WHERE deployment_id = ? AND student_id = ? AND root_id = ?
--
CREATE TABLE IF NOT EXISTS learner.student_scope_tree_by_element (
    deployment_id timeuuid,
    student_id timeuuid,
    root_id timeuuid,
    scope_urn timeuuid,
    scope_id timeuuid,
    element_id timeuuid,
    element_type text,
    PRIMARY KEY ((deployment_id, student_id, root_id), scope_urn)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
