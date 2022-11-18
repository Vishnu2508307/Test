--
-- learner.deployment_by_asset
-- --------------------
-- Tracks all the deployments an asset is used in
--
-- Find all the deployments for a given asset
-- > SELECT * FROM learner.deployment_by_asset WHERE asset_id = ?
--
CREATE TABLE IF NOT EXISTS learner.deployment_by_asset (
    asset_id timeuuid,
    deployment_id timeuuid,
    change_id timeuuid,
    PRIMARY KEY (asset_id, deployment_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
