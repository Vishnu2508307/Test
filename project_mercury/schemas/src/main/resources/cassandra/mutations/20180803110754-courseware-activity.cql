--
-- courseware.activity
-- -------------------
-- Keep high level activity information
--
-- Find an activity by id
-- > SELECT * FROM courseware.activity WHERE id = ?
--
CREATE TABLE IF NOT EXISTS courseware.activity (
    id timeuuid,
    plugin_id timeuuid,
    plugin_version_expr text,
    PRIMARY KEY(id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
