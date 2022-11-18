--
-- learner.asset_by_deployment
-- --------------------
-- Tracks all the assets used in a deployment
--
-- Find all the assets for a deployment
-- > SELECT * FROM learner.asset_by_deployment WHERE deployment_id = ? AND change_id = ?
--
CREATE TABLE IF NOT EXISTS learner.asset_by_deployment (
    deployment_id timeuuid,
    change_id timeuuid,
    asset_id timeuuid,
    PRIMARY KEY (deployment_id, change_id, asset_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
