--
-- learner.progress_by_courseware
-- ------------------------------
-- Track progress by courseware element id, ordered newest to oldest
--
-- Find the progress of a courseware element id on a deployment for a student
-- > SELECT * FROM WHERE learner.courseware_element_id = ? AND deployment_id = ? AND student_id = ?;
--
CREATE TABLE IF NOT EXISTS learner.progress_by_courseware (
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
    PRIMARY KEY((deployment_id, courseware_element_id, student_id), id)
)
WITH CLUSTERING ORDER BY (id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
