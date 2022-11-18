--
-- export.error_notification
-- -------------------------
-- This table store the error log for courseware export.
--
-- Find the error log details for courseware export
-- > SELECT * FROM export.error_notification WHERE notification_id =?
--
CREATE TABLE IF NOT EXISTS export.error_notification (
    notification_id timeuuid,
    error_message text,
    cause text,
    id timeuuid,
    PRIMARY KEY (notification_id, id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
