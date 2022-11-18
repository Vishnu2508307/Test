--
-- learner.deployment_by_asset_urn
-- -------------------------------
-- Tracks how many deployment an asset urn is used in
--
-- Find all the deployments an asset urn is reference in
-- > SELECT * FROM learner.deployment_by_asset_urn WHERE asset_urn = ?
--
CREATE TABLE IF NOT EXISTS learner.deployment_by_asset_urn (
    asset_urn text,
    deployment_id timeuuid,
    change_id timeuuid,
    PRIMARY KEY (asset_urn, deployment_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
