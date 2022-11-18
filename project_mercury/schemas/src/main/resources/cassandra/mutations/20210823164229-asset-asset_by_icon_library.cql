--
-- asset.asset_by_icon_library
-- --------------------
-- Stores information about all the icon asset_urn, metadata with their icon_library name
--
-- Find all the asset_urn's by icon_library name
-- > SELECT * FROM asset.asset_by_icon_library WHERE icon_library = 'MICROSOFT_ICON'
--
CREATE TABLE IF NOT EXISTS asset.asset_by_icon_library (
    icon_library text,
    asset_urn timeuuid,
    metadata text,
    PRIMARY KEY (icon_library, asset_urn)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
