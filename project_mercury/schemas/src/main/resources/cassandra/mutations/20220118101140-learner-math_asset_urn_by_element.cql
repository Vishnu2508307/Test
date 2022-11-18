--
-- learner.math_asset_urn_by_element
-- --------------------
-- Tracks the relationship between courseware elements and math assetUrn
--
-- Find all the asset urn that belong to a courseware element
-- > SELECT * FROM learner.math_asset_urn_by_element WHERE element_id = ? AND change_id = ?
--
CREATE TABLE IF NOT EXISTS learner.math_asset_urn_by_element (
    element_id timeuuid,
    change_id timeuuid,
    asset_urn text,
    element_type text,
    PRIMARY KEY (element_id, change_id, asset_urn)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
