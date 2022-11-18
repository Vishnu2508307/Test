--
-- export.request_notification
-- ---------------------------
-- This table store the request notification for courseware export
--
-- Find the request notification for a courseware export
-- > SELECT * FROM export.request_notification WHERE notification_id = ?
--
CREATE TABLE IF NOT EXISTS export.request_notification (
    notification_id timeuuid,
    element_id timeuuid,
    account_id timeuuid,
    workspace_id timeuuid,
    element_type text,
    status text,
    project_id timeuuid,
    primary key (notification_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
