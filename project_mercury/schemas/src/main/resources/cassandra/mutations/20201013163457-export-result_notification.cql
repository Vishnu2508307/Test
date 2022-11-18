--
-- export.result_notification
-- --------------------------
-- This table store the result notification for courseware export.
--
-- Find the result notification for a courseware export
-- > SELECT * FROM export.result_notification WHERE notification_id =?
--
CREATE TABLE IF NOT EXISTS export.result_notification (
    element_id timeuuid,
    account_id timeuuid,
    notification_id timeuuid,
    workspace_id timeuuid,
    project_id timeuuid,
    element_type text,
    status text,
    body text,
    completed_at timeuuid,
    primary key (notification_id)
)WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
