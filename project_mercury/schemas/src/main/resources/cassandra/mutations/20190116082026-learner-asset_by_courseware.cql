--
-- learner.asset_by_courseware
-- --------------------
-- Used for tracking assets by courseware elements
--
-- Find all asset that an element is using
-- > SELECT * FROM learner.asset_by_courseware WHERE element_id = ?
--
CREATE TABLE IF NOT EXISTS learner.asset_by_courseware (
    element_id timeuuid,
    change_id timeuuid,
    asset_id timeuuid,
    PRIMARY KEY (element_id, change_id, asset_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
