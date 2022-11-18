--
-- learner.evaluable
-- --------------------
-- This table is designed to store the evaluation mode.
--
-- Find evaluation mode of a courseware element by given element_id, deployment_id and change_id
-- > SELECT * FROM learner.evaluable WHERE element_id = ? and deployment_id = ? and change_id = ?
--
CREATE TABLE IF NOT EXISTS learner.evaluable (
    element_id timeuuid,
    deployment_id timeuuid,
    change_id timeuuid,
    element_type text,
    evaluation_mode text,
    PRIMARY KEY (element_id, deployment_id, change_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
