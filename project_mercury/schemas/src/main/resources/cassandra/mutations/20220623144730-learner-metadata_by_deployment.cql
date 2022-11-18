--
-- learner.metadata_by_deployment
-- --------------------
-- This table store deployment_id, change_id and element_id as part of deployment if the scenario has GRADE_PASSBACK action.
--
-- Fetch the row count by deployment_id and change_id
-- > SELECT * FROM learner.metadata_by_deployment WHERE deployment_id = ? AND change_id = ?;
--
CREATE TABLE IF NOT EXISTS learner.metadata_by_deployment (
    deployment_id timeuuid,
    change_id timeuuid,
    element_id timeuuid,
    PRIMARY KEY ((deployment_id, change_id), element_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};

