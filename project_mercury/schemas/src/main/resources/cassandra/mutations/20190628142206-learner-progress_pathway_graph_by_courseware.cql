--
-- learner.progress_pathway_graph_by_courseware
-- --------------------------------------------
-- Track graph progress by courseware element id, ordered newest to oldest
--
-- Find the graph progress for a courseware element id on a deployment for a student
-- > SELECT * FROM learner.progress_pathway_graph_by_courseware WHERE deployment_id = ? AND student_id = ? AND courseware_element_id = ?
--
CREATE TABLE IF NOT EXISTS learner.progress_pathway_graph_by_courseware (
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
    current_walkable_id timeuuid,
    current_walkable_type text,
    PRIMARY KEY((deployment_id, courseware_element_id, student_id), id)
) WITH CLUSTERING ORDER BY (id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
