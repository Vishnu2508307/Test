--
-- learner.progress_pathway_graph
-- ------------------------------
-- Store graph pathway progresses
--
-- Find a graph pathway progress by id
-- > SELECT * FROM learner.progress_pathway_graph WHERE id = ?
--
CREATE TABLE IF NOT EXISTS learner.progress_pathway_graph (
    id timeuuid,
    attempt_id timeuuid,
    change_id timeuuid,
    child_completed list<timeuuid>,
    child_completion_confidences map<timeuuid, float>,
    child_completion_values map<timeuuid, float>,
    completion_confidence float,
    completion_value float,
    courseware_element_id timeuuid,
    courseware_element_type text,
    deployment_id timeuuid,
    evaluation_id timeuuid,
    student_id timeuuid,
    current_walkable_id timeuuid,
    current_walkable_type text,
    PRIMARY KEY(id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
