--
-- learner.progress
-- ----------------
-- Track learner progress by Progress id
--
-- Find Progress by id
-- > SELECT * FROM learner.progress WHERE id = ?
--
CREATE TABLE IF NOT EXISTS learner.progress (
    id timeuuid,
    deployment_id timeuuid,
    change_id timeuuid,
    courseware_element_id timeuuid,
    courseware_element_type text,
    student_id timeuuid,
    attempt_id timeuuid,
    evaluation_id timeuuid,
    completion_value float,
    completion_confidence float,
    PRIMARY KEY(id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
