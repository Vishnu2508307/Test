--
-- courseware.interactive_config
-- --------------------
-- Store the configuration for an interactive, in a log appended format (for history), latest is on top.
-- TODO: this should be replaced by an interactive_config_diff table that only contains diff changes
--
-- Find the latest interactive config
-- > SELECT * FROM courseware.interactive_config WHERE interactive_id = ? LIMIT 1
--
CREATE TABLE IF NOT EXISTS courseware.interactive_config (
    id timeuuid,
    interactive_id timeuuid,
    config text,
    PRIMARY KEY(interactive_id, id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
