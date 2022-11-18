--
-- courseware.asset_by_courseware
-- --------------------
-- Tracks assets by courseware elements
--
-- Select all assets which are used in a courseware element
-- > SELECT * FROM courseware.asset_by_courseware WHERE element_id = ?
--
CREATE TABLE IF NOT EXISTS courseware.asset_by_courseware (
    element_id timeuuid,
    asset_id timeuuid,
    PRIMARY KEY (element_id, asset_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
