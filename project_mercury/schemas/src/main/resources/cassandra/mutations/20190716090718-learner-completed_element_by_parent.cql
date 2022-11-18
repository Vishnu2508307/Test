--
-- learner.completed_element_by_parent
-- ------------------------------------
-- Track all the completed walkable elements by a student for a parent courseware element. It is important to keep the
-- ordering, this is achieved with a clustering column ordering on evaluation_id.ALL. This table is to replace
-- completed_walkable_by_parent which should not be used. The main difference is that this does not include change_id
-- in the primary key
--
-- The same walkable element can appear multiple times but the walkable_attempt_id is expected to be different. This table
-- gives priority over enforcing ordering rather than uniqueness on element_attempt_id.
--
-- Find all the completed walkable elements by a student on a particular deployment for a parent courseware element
-- > SELECT * FROM learner.completed_element_by_parent WHERE deployment_id = ?
-- AND student_id = ?
-- AND parent_element_id = ?
-- AND parent_element_attempt_id = ?
--
CREATE TABLE IF NOT EXISTS learner.completed_element_by_parent (
    deployment_id timeuuid,
    change_id timeuuid,
    student_id timeuuid,
    parent_element_id timeuuid,
    parent_element_attempt_id timeuuid,
    element_id timeuuid,
    evaluation_id timeuuid,
    element_attempt_id timeuuid,
    parent_element_type text,
    element_type text,
    PRIMARY KEY ((deployment_id, student_id, parent_element_id, parent_element_attempt_id), evaluation_id, element_id, element_attempt_id)
) WITH CLUSTERING ORDER BY (evaluation_id DESC)
AND compression = {
'sstable_compression' : 'SnappyCompressor'
};
