--
-- learner.asset_urn_by_deployment
-- -------------------------------
-- Tracks all the asset urn that exists in a deployment
--
-- Find all the asset urn in a deployment
-- > SELECT * FROM learner.asset_urn_by_deployment WHERE deployment_id = ? AND change_id = ?
--
CREATE TABLE IF NOT EXISTS learner.asset_urn_by_deployment (
    deployment_id timeuuid,
    change_id timeuuid,
    asset_urn text,
    PRIMARY KEY (deployment_id, change_id, asset_urn)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
