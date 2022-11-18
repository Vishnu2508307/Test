--
-- plugin.generic_log_statement_by_plugin
-- ---------------------------------------
-- This table stores generic log statements.
--
-- Find all logs for a plugin version, by day time filterable by level.
-- > SELECT * FROM plugin.generic_log_statement_by_plugin WHERE plugin_id = ? AND version = ? AND bucket_id = ?;
--
CREATE TABLE IF NOT EXISTS plugin.generic_log_statement_by_plugin (
    plugin_id       timeuuid,
    version         text,
    bucket_id       timeuuid,
    level           text,
    id              timeuuid,
    message         text,
    args            text,
    plugin_context  text,
    logging_context text,
    PRIMARY KEY ((plugin_id, version, bucket_id), level, id)
)
WITH CLUSTERING ORDER BY (level DESC, id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
