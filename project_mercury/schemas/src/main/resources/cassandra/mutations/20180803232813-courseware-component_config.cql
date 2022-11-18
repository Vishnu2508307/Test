--
-- courseware.component_config
-- ---------------------------
-- Store the configuration for a component, in a log appended format (for history), latest is on top.
--
-- Find the latest component config
-- > SELECT * FROM courseware.component_config WHERE id = ? LIMIT 1
--
CREATE TABLE IF NOT EXISTS courseware.component_config (
    id timeuuid,
    component_id timeuuid,
    config text,
    PRIMARY KEY(component_id, id)
)
WITH CLUSTERING ORDER BY (id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
