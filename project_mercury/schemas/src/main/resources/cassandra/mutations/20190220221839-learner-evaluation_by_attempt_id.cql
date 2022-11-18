--
-- learner.evaluation_by_attempt_id
-- --------------------------------
-- Find an evaluation result by using an attempt id
--
-- Find an evaluation result by attempt id
-- > SELECT * FROM learner.evaluation_by_attempt_id WHERE attempt_id = ?;
--
CREATE TABLE IF NOT EXISTS learner.evaluation_by_attempt_id (
    id timeuuid,
    courseware_element_id timeuuid,
    courseware_change_id timeuuid,
    deployment_id timeuuid,
    attempt_id timeuuid,
    interactive_complete boolean,
    PRIMARY KEY (attempt_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
