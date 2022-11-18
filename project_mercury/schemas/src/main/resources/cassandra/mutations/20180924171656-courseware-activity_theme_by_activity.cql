--
-- courseware.activity_theme_by_activity
-- --------------------
-- Stores the relationship between an activity and a theme
--
-- Get the theme from activity
-- > SELECT * FROM courseware.activity_theme_by_activity WHERE activity_id=?
--
CREATE TABLE IF NOT EXISTS courseware.activity_theme_by_activity (
    activity_id timeuuid,
    theme_id timeuuid,
    PRIMARY KEY(activity_id,theme_id)
)
WITH CLUSTERING ORDER BY (theme_id ASC) AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
