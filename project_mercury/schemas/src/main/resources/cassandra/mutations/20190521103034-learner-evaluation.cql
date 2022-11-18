--
-- learner.evaluation_result
-- -------------------------
-- Store an enriched version of the evaluation result.
--
-- The element_scope_data_map is a map of source_id and json data. Source id representing the id of the registered element
-- that wrote such data to the scope of the element being evaluated.
--
-- Find an evaluation result by id
-- > SELECT * FROM learner.evaluation_result WHERE id = ?
--
CREATE TABLE IF NOT EXISTS learner.evaluation (
    id timeuuid,
    element_id timeuuid,
    element_type text,
    deployment_id timeuuid,
    change_id timeuuid,
    student_id timeuuid,
    attempt_id timeuuid,
    element_scope_data_map map<timeuuid, text>,
    student_scope_urn timeuuid,
    parent_id timeuuid,
    parent_type text,
    parent_attempt_id timeuuid,
    completed boolean,
    triggered_scenario_ids list<timeuuid>,
    scenario_correctness text,
    PRIMARY KEY (id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
