--
-- export.result_notification_by_export
-- --------------------
-- This table store the result notification details.
--
-- Find all result notifications by export id
-- > SELECT * FROM export.result_notification_by_export WHERE export_id=?
--
CREATE TABLE IF NOT EXISTS export.result_notification_by_export (
    export_id timeuuid,
    notification_id timeuuid,
    element_id timeuuid,
    account_id timeuuid,
    workspace_id timeuuid,
    project_id timeuuid,
    element_type text,
    status text,
    completed_at timeuuid,
    primary key (export_id, notification_id)
)WITH CLUSTERING ORDER BY (notification_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
