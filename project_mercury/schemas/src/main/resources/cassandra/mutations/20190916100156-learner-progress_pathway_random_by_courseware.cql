--
-- learner.progress_pathway_random_by_courseware
-- ---------------------------------------------
-- Tracks student progresses on random pathways by courseware element
--
-- Write a description of the query that this table supports
-- > SELECT * FROM learner.progress_pathway_random_by_courseware WHERE deployment_id = ? AND student_id = ? AND courseware_element_id = ?
--
CREATE TABLE IF NOT EXISTS learner.progress_pathway_random_by_courseware (
    deployment_id timeuuid,
    courseware_element_id timeuuid,
    student_id timeuuid,
    id timeuuid,
    attempt_id timeuuid,
    change_id timeuuid,
    child_completed list<timeuuid>,
    child_completion_confidences map<timeuuid, float>,
    child_completion_values map<timeuuid, float>,
    completion_confidence float,
    completion_value float,
    courseware_element_type text,
    evaluation_id timeuuid,
    in_progress_element_id timeuuid,
    in_progress_element_type text,
    PRIMARY KEY ((deployment_id, courseware_element_id, student_id), id)
) WITH CLUSTERING ORDER BY (id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
