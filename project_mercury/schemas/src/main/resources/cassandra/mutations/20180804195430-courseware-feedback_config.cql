--
-- courseware.feedback_config
-- --------------------------
-- Store feedback configuration
--
-- Find the configuration for a feedback
-- > SELECT * FROM courseware.feedback_config WHERE feedback_id = ?
--
CREATE TABLE IF NOT EXISTS courseware.feedback_config (
    id timeuuid,
    feedback_id timeuuid,
    config text,
    PRIMARY KEY(feedback_id, id)
)
WITH CLUSTERING ORDER BY (id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
