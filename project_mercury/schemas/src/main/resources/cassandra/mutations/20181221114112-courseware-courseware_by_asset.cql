--
-- courseware.courseware_by_asset
-- --------------------
-- Tracks usage of assets in courseware elements
--
-- Find all courseware element where asset is used
-- > SELECT * FROM courseware.courseware_by_asset WHERE asset_id = ?
--
CREATE TABLE IF NOT EXISTS courseware.courseware_by_asset (
    asset_id timeuuid,
    element_id uuid,
    element_type text,
    PRIMARY KEY (asset_id, element_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
