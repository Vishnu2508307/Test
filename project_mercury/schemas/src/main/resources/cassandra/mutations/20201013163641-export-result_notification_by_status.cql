--
-- export.result_notification_by_status
-- --------------------
-- This table store the result notification for courseware export by export status.
--
-- Find the result notification for a courseware export by status
-- > SELECT * FROM export.result_notification_by_status WHERE notification_id = ? and status = ?
--
CREATE TABLE IF NOT EXISTS export.result_notification_by_status (
    element_id timeuuid,
    account_id timeuuid,
    notification_id timeuuid,
    workspace_id timeuuid,
    project_id timeuuid,
    element_type text,
    status text,
    body text,
    completed_at timeuuid,
    primary key (status, notification_id)
) WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
