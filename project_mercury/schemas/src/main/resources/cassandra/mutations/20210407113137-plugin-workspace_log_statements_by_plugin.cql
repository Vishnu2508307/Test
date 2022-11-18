--
-- plugin.workspace_log_statements_by_plugin
-- -----------------------------------------
-- This table stores workspace log statements.
--
-- Find all logs for a plugin version, by day time filterable by level, logged in the workspace.
-- > SELECT * FROM plugin.workspace_log_statements_by_plugin WHERE plugin_id = ? AND version = ? AND bucket_id = ?;
--
CREATE TABLE IF NOT EXISTS plugin.workspace_log_statements_by_plugin (
    plugin_id timeuuid,
    version text,
    bucket_id timeuuid,
    level text,
    id timeuuid,
    message text,
    args text,
    project_id timeuuid,
    element_id timeuuid,
    element_type text,
    plugin_context text,
    PRIMARY KEY((plugin_id, version, bucket_id), level, id)
)
WITH CLUSTERING ORDER BY (level DESC, id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
