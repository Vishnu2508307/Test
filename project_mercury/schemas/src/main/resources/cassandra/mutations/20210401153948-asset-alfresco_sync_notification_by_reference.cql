--
-- asset.alfresco_sync_notification_by_reference
-- -------------------------------------
-- This table store the notifications for alfresco course asset syncs by sync reference id.
--
-- Find the notification for an alfresco course asset sync by reference id
-- > SELECT * FROM asset.alfresco_sync_notification_by_reference WHERE reference_id = ?
--
CREATE TABLE IF NOT EXISTS asset.alfresco_sync_notification_by_reference (
    reference_id timeuuid,
    notification_id timeuuid,
    course_id timeuuid,
    asset_id timeuuid,
    sync_type text,
    status text,
    completed_at timeuuid,
    primary key (reference_id, notification_id)
) WITH CLUSTERING ORDER BY (notification_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
