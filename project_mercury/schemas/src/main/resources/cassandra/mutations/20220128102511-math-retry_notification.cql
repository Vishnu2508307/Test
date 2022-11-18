--
-- math.retry_notification
-- --------------------
-- Track the results of the scheduled Retry notifications
--
-- Find the given retry notification
-- > SELECT * FROM math.retry_notification WHERE notification_id=?
--
CREATE TABLE IF NOT EXISTS math.retry_notification (
   notification_id timeuuid,
   source_notification_id timeuuid,
   delay_sec bigint,
   PRIMARY KEY (notification_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
