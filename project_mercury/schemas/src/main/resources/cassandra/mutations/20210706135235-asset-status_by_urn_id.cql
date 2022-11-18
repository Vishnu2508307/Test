--
-- asset.status_by_urn_id
-- --------------------
-- This table stores asset status information by asset urn and asset id
--
-- Fetch asset status information by asset_urn and asset id
-- > SELECT * FROM asset.status_by_urn_id WHERE asset_urn=? AND asset_id=?
--
CREATE TABLE IF NOT EXISTS asset.status_by_urn_id (
    id        timeuuid,
    asset_urn text,
    asset_id  timeuuid,
    status    text,
    PRIMARY KEY ((asset_urn, asset_id), id)
) WITH CLUSTERING ORDER BY (id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
