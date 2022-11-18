--
-- learner.score_entry_by_element
-- --------------------
-- Store the student score entries for each element and attempt.
--
-- Find all the score entries for a student over a particular element attempt
-- > SELECT * FROM learner.score_entry_by_element
-- WHERE deployment_id = ?
-- AND student_id = ?
-- AND element_id = ?
-- AND attempt_id = ?
--
CREATE TABLE IF NOT EXISTS learner.score_entry_by_element (
    cohort_id timeuuid,
    deployment_id timeuuid,
    change_id timeuuid,
    student_id timeuuid,
    element_id timeuuid,
    attempt_id timeuuid,
    id timeuuid,
    value double,
    adjustment_value double,
    evaluation_id timeuuid,
    operator text,
    source_element_id timeuuid,
    source_scenario_id timeuuid,
    source_account_id timeuuid,
    element_type text,
    PRIMARY KEY ((deployment_id, student_id, element_id, attempt_id), id)
) WITH CLUSTERING ORDER BY (id ASC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
