--
-- courseware.interactive_config_latest
-- Store the configuration for an interactive, in a log appended format (for history), latest is on top.

CREATE TABLE IF NOT EXISTS courseware.interactive_config_latest (
    interactive_id timeuuid,
    id timeuuid,
    config text,
    PRIMARY KEY((interactive_id, id))
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
