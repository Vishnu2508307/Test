--
-- learner.courseware_by_asset
-- --------------------
-- Used to track courseware elements by asset
--
-- Find all elements that are using a given asset
-- > SELECT * FROM learner.courseware_by_asset WHERE asset_id = ?
--
CREATE TABLE IF NOT EXISTS learner.courseware_by_asset (
    asset_id timeuuid,
    deployment_id timeuuid,
    change_id timeuuid,
    element_id timeuuid,
    element_type text,
    PRIMARY KEY (asset_id, deployment_id, change_id, element_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
