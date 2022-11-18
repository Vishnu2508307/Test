--
-- learner.progress_pathway_bkt
-- ----------------------------
-- Tracks progresses on BKT pathways
--
-- Find the progress by id
-- > SELECT * FROM learner.progress_pathway_bkt WHERE id = ?
--
CREATE TABLE IF NOT EXISTS learner.progress_pathway_bkt (
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
    in_progress_element_id timeuuid,
    in_progress_element_type text,
    student_id timeuuid,
    p_ln_minus_given_actual double,
    p_ln double,
    p_correct double,
    PRIMARY KEY (id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
