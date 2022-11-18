--
-- asset.icons_by_library
-- --------------------
-- Stores information about icon type asset
--
-- Find all the asset_urn's by icon_library name
-- > SELECT * FROM asset.icons_by_library WHERE icon_library = 'MICROSOFT_ICON'
--
CREATE TABLE IF NOT EXISTS asset.icons_by_library (
    icon_library text,
    asset_urn text,
    metadata text,
    PRIMARY KEY (icon_library, asset_urn)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
