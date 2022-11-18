--
-- asset.status_by_urn
-- --------------------
-- This table stores asset status information by asset urn
--
-- Fetch asset status information by asset_urn
-- > SELECT * FROM asset.status_by_urn WHERE asset_urn = ?
--
CREATE TABLE IF NOT EXISTS asset.status_by_urn (
    id        timeuuid,
    asset_urn text,
    asset_id  timeuuid,
    status    text,
    PRIMARY KEY (asset_urn, id)
) WITH CLUSTERING ORDER BY (id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
