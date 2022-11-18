--
-- learner.progress_pathway_linear_by_courseware
-- ---------------------------------------------
-- Track linear progress by courseware element id, ordered newest to oldest
--
-- Find the linear progress for a courseware element id on a deployment for a student
-- > SELECT * FROM learner.progress_pathway_linear_by_courseware WHERE deployment_id = ? AND student_id = ? AND courseware_element_id = ?
--
CREATE TABLE IF NOT EXISTS learner.progress_pathway_linear_by_courseware (
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
    child_completed list<timeuuid>,
    PRIMARY KEY((deployment_id, courseware_element_id, student_id), id)
)
WITH CLUSTERING ORDER BY (id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
