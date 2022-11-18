--
-- export.summary_by_workspace
-- ---------------------------
-- This table store the export summary details by workspace
--
--  Find the export summary details for given workspace id
-- > SELECT * FROM export.summary_by_workspace WHERE workspace_id=?
--
CREATE TABLE IF NOT EXISTS export.summary_by_workspace (
    workspace_id timeuuid,
    project_id timeuuid,
    export_id timeuuid,
    element_id timeuuid,
    account_id timeuuid,
    element_type text,
    status text,
    completed_at timeuuid,
    primary key (workspace_id, project_id, export_id, element_id)
)WITH CLUSTERING ORDER BY (project_id DESC, export_id DESC, element_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};