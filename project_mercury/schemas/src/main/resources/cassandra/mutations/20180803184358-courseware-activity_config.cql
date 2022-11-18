--
-- courseware.activity_config
-- --------------------------
-- Store the configuration for an activity, in a log appended format (for history), latest is on top.
-- TODO: this should be replaced by an activity_config_diff table that only contains diff changes
--
-- Find the latest activity config
-- > SELECT * FROM courseware.activity_config WHERE activity_id = ? LIMIT 1
--
CREATE TABLE IF NOT EXISTS courseware.activity_config (
    id timeuuid,
    activity_id timeuuid,
    config text,
    PRIMARY KEY(activity_id, id)
)
WITH CLUSTERING ORDER BY (id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
