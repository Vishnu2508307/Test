--
-- math.asset_result_notification_by_asset
-- --------------------
-- This table store the result notification details.
--
-- Find all result notifications by export id
-- > SELECT * FROM math.asset_result_notification_by_asset WHERE asset_id = ?
--
CREATE TABLE IF NOT EXISTS math.asset_result_notification_by_asset (
    asset_id timeuuid,
    notification_id timeuuid,
    math_ml text,
    alt_text text,
    svg_shape text,
    primary key (asset_id, notification_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
