--
-- learner.courseware_by_asset_urn
-- -------------------------------
-- Tracks the relationship between a courseware element and an assetUrn
--
-- Find all the courseware elements that are associated to an assetUrn in a deployment
-- > SELECT * FROM learner.courseware_by_asset_urn WHERE asset_urn = ?
-- AND deployment_id = ? AND change_id = ?
--
CREATE TABLE IF NOT EXISTS learner.courseware_by_asset_urn (
    asset_urn text,
    deployment_id timeuuid,
    change_id timeuuid,
    element_id timeuuid,
    element_type text,
    PRIMARY KEY (asset_urn, deployment_id, change_id, element_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};