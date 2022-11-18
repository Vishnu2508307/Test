--
-- math.error_notification_by_asset
-- --------------------
-- Store notification errors that can be queried given an asset id
--
-- Find all notification error for an export
-- > SELECT * FROM math.error_notification_by_asset WHERE asset_id = ?
--
CREATE TABLE IF NOT EXISTS math.error_notification_by_asset (
    asset_id timeuuid,
    notification_id timeuuid,
    cause text,
    error_message text,
    PRIMARY KEY (asset_id, notification_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
