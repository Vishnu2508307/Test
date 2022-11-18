--
-- learner.completed_element
-- --------------------
-- Track all the completed walkable elements by a student. The ordering is determined by the element_attempt_id. The same
-- deployment_id, element_id can be completed by the same student_id, however the element_attempt_id will always be different.
-- The only way there can be multiple records per deployment_id, student_id, element_id is if the parent attempt has changed.
--
-- Find all the times a student has completed the same walkable
-- > SELECT * FROM learner.completed_element WHERE deployment_id = ? AND student_id = ? AND element_id = ?
--
-- Find the completed element information by attempt
-- > SELECT * FROM learner.completed_element WHERE deployment_id = ?
-- AND student_id = ?
-- AND element_id = ?
-- AND element_attempt_id = ?
--
CREATE TABLE IF NOT EXISTS learner.completed_element (
    deployment_id timeuuid,
    change_id timeuuid,
    student_id timeuuid,
    element_id timeuuid,
    element_attempt_id timeuuid,
    evaluation_id timeuuid,
    parent_element_id timeuuid,
    parent_element_attempt_id timeuuid,
    parent_element_type text,
    element_type text,
    PRIMARY KEY ((deployment_id, student_id, element_id), element_attempt_id)
) WITH CLUSTERING ORDER BY (element_attempt_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
