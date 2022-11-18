--
-- courseware.feedback
-- -------------------
-- Track feedback
--
-- Find a feedback by id
-- > SELECT * FROM courseware.feedback WHERE id = ?
--
CREATE TABLE IF NOT EXISTS courseware.feedback (
    id timeuuid,
    interactive_id timeuuid,
    plugin_id timeuuid,
    plugin_version_expr text,
    PRIMARY KEY (id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
