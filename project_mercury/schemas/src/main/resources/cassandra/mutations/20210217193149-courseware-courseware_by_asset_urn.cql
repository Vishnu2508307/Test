--
-- courseware.courseware_by_asset_urn
-- ----------------------------------
-- Tracks the relationship between a courseware element and an assetUrn
--
-- Find all the courseware elements that are associated to an assetUrn
-- > SELECT * FROM courseware.courseware_by_asset_urn WHERE asset_urn = ?
--
CREATE TABLE IF NOT EXISTS courseware.courseware_by_asset_urn (
    asset_urn text,
    element_id timeuuid,
    element_type text,
    PRIMARY KEY (asset_urn, element_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
