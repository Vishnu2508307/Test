--
-- export.result_notification_by_workspace
-- ---------------------------------------
-- This table store the result notification for courseware export by export workspace id.
--
-- Find the result notification for a courseware export by workspace id
-- > SELECT * FROM export.result_notification_by_workspace WHERE notification_id = ? and workspace_id = ?
--
CREATE TABLE IF NOT EXISTS export.result_notification_by_workspace (
    element_id timeuuid,
    account_id timeuuid,
    notification_id timeuuid,
    workspace_id timeuuid,
    project_id timeuuid,
    element_type text,
    status text,
    body text,
    completed_at timeuuid,
    primary key ((workspace_id), project_id, notification_id, element_id)
) WITH CLUSTERING ORDER BY (project_id DESC, notification_id DESC, element_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
