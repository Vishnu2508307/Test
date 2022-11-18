--
-- courseware.interactive_config_id
-- Store the configuration for an interactive, in a log appended format (for history), latest is on top.
-- Find the latest interactive config id
-- > SELECT * FROM courseware.interactive_config_id WHERE interactive_id = ? LIMIT 1

CREATE TABLE IF NOT EXISTS courseware.interactive_config_id (
    interactive_id timeuuid,
    config_id timeuuid,
    PRIMARY KEY(interactive_id, config_id)
)
WITH CLUSTERING ORDER BY (config_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
