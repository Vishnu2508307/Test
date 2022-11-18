--
-- asset.request_notification_by_asset
-- --------------------
-- This table store the request notification details.
--
-- Find all request notifications by export id
-- > SELECT * FROM asset.request_notification_by_asset WHERE asset_id = ?
--
CREATE TABLE IF NOT EXISTS asset.request_notification_by_asset (
    asset_id timeuuid,
    notification_id timeuuid,
    url text,
    original_width double,
    original_height double,
    threshold double,
    size text,
    primary key (asset_id, notification_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
