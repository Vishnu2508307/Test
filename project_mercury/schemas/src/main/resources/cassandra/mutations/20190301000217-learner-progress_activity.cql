--
-- learner.progress_activity
-- -------------------------
-- Track activity progress by id
--
-- Find an activity progress by id
-- > SELECT * FROM learner.progress_activity WHERE id = ?
--
CREATE TABLE IF NOT EXISTS learner.progress_activity (
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
    child_completion_values map<timeuuid, float>,
    child_completion_confidences map<timeuuid, float>,
    PRIMARY KEY(id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
