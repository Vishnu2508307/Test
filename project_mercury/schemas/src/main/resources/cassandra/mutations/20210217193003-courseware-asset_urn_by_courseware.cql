--
-- courseware.asset_urn_by_courseware
-- ----------------------------------
-- Tracks the relationship between courseware elements and assetUrn
--
-- Find all the asset urn that belong to a courseware element
-- > SELECT * FROM courseware.asset_urn_by_courseware WHERE element_id = ?
--
CREATE TABLE IF NOT EXISTS courseware.asset_urn_by_courseware (
    element_id timeuuid,
    asset_urn text,
    element_type text,
    PRIMARY KEY (element_id, asset_urn)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
