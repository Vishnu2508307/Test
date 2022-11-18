--
-- export.summary_by_project
-- -------------------------
-- This table store the export summary details by project
--
--  Find the export summary details for given project id
-- > SELECT * FROM export.summary_by_project WHERE project_id=?
--
CREATE TABLE IF NOT EXISTS export.summary_by_project (
    project_id timeuuid,
    export_id timeuuid,
    element_id timeuuid,
    account_id timeuuid,
    workspace_id timeuuid,
    element_type text,
    status text,
    completed_at timeuuid,
    primary key (project_id, export_id, element_id)
)WITH CLUSTERING ORDER BY (export_id DESC, element_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
