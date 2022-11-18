--
-- courseware.asset_urn_by_root_activity
-- -------------------------------------
-- Tracks all the assetUrn that belong to a root activity
--
-- Find all the assetUrn that belong to a root activity
-- > SELECT * FROM courseware.asset_urn_by_root_activity WHERE root_activity_id = ?
--
CREATE TABLE IF NOT EXISTS courseware.asset_urn_by_root_activity (
    root_activity_id timeuuid,
    asset_urn text,
    element_id timeuuid,
    element_type text,
    PRIMARY KEY (root_activity_id, asset_urn, element_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
