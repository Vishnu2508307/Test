--
-- export.result_notification_by_project
-- -------------------------------------
-- This table store the result notification for courseware export by export project id.
--
-- Find the result notification for a courseware export by project id
-- > SELECT * FROM export.result_notification_by_project WHERE notification_id = ? and project_id = ?
--
CREATE TABLE IF NOT EXISTS export.result_notification_by_project (
    element_id timeuuid,
    account_id timeuuid,
    notification_id timeuuid,
    workspace_id timeuuid,
    project_id timeuuid,
    element_type text,
    status text,
    body text,
    completed_at timeuuid,
    primary key ((project_id), element_id, notification_id)
) WITH CLUSTERING ORDER BY (element_id DESC, notification_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
