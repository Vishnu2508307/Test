--
-- ext_http.retry_notification
-- ---------------------------
-- Track the results of the scheduled Retry notifications
--
-- Find the given retry notification
-- > SELECT * FROM ext_http.retry_notification WHERE notification_id = ?;
--
CREATE TABLE IF NOT EXISTS ext_http.retry_notification (
    notification_id timeuuid,
    source_notification_id timeuuid,
    reference_id timeuuid,
    purpose text,
    delay_sec bigint,
    PRIMARY KEY ( notification_id )
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
