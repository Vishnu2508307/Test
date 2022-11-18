--
-- courseware.activity_theme
-- --------------------
-- Stores the theme config for an activity
--
-- Get the latest theme for an activity
-- > SELECT * FROM courseware.activity_theme WHERE activity_id = ? LIMIT 1
--
CREATE TABLE IF NOT EXISTS courseware.activity_theme (
    activity_id timeuuid,
    id timeuuid,
    config text,
    PRIMARY KEY (activity_id, id)
)
WITH CLUSTERING ORDER BY (id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
