--
-- export.error_notification_by_export
-- -----------------------------------
-- Store notification errors that can be queried given an export id
--
-- Find all notification error for an export
-- > SELECT * FROM export.error_notification_by_export WHERE export_id = ?
--
CREATE TABLE IF NOT EXISTS export.error_notification_by_export (
    export_id timeuuid,
    notification_id timeuuid,
    cause text,
    error_message text,
    PRIMARY KEY (export_id, notification_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
