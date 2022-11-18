--
-- math.asset_request_notification_by_asset
-- --------------------
-- This table store the request notification details.
--
-- Find all request notifications by export id
-- > SELECT * FROM math.asset_request_notification_by_asset WHERE asset_id = ?
--
CREATE TABLE IF NOT EXISTS math.asset_request_notification_by_asset (
    asset_id timeuuid,
    notification_id timeuuid,
    math_ml text,
    primary key (asset_id, notification_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
