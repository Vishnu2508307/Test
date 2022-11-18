--
-- math.asset_urn_by_element
-- --------------------
-- Tracks the relationship between courseware elements and math assetUrn
--
-- Find all the math asset urn that belong to a courseware element
-- > SELECT * FROM math.asset_urn_by_element WHERE element_id = ?
--
CREATE TABLE IF NOT EXISTS math.asset_urn_by_element (
    element_id timeuuid,
    asset_urn text,
    PRIMARY KEY (element_id, asset_urn)
)
WITH compression = {
   'sstable_compression' : 'SnappyCompressor'
};
