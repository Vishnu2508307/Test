--
-- asset.alfresco_sync_notification
-- --------------------------
-- This table store the notifications for alfresco syncs.
--
-- Find the notification for an alfresco sync
-- > SELECT * FROM asset.alfresco_sync_notification WHERE notification_id =?
--
CREATE TABLE IF NOT EXISTS asset.alfresco_sync_notification (
    notification_id timeuuid,
    reference_id timeuuid,
    course_id timeuuid,
    asset_id timeuuid,
    sync_type text,
    status text,
    completed_at timeuuid,
    primary key (notification_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
