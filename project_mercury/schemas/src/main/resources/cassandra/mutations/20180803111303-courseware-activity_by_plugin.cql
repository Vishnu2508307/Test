--
-- courseware.activity_by_plugin
-- -----------------------------
-- Track plugin usage by activity
--
-- Find all the activities using a particular plugin
-- > SELECT * FROM activity_by_plugin WHERE plugin_id = ?;
--
CREATE TABLE IF NOT EXISTS courseware.activity_by_plugin (
    plugin_id timeuuid,
    activity_id timeuuid,
    plugin_version_expr text,
    PRIMARY KEY(plugin_id, activity_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
