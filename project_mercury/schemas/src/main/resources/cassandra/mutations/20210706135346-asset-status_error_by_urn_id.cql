--
-- asset.status_error_by_urn_id
-- --------------------
-- This table stores asset status error information
--
-- Fetch asset status error details by asset_urn and asset_id
-- > SELECT * FROM asset.status_error_by_urn_id WHERE asset_urn=? AND asset_id=?
--
CREATE TABLE IF NOT EXISTS asset.status_error_by_urn_id (
    id        timeuuid,
    asset_urn text,
    asset_id  timeuuid,
    status    text,
    error_cause text,
    error_message text,
    PRIMARY KEY ((asset_urn, asset_id), id)
) WITH CLUSTERING ORDER BY (id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
