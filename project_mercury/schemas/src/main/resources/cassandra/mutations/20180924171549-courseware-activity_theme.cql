--
-- courseware.activity_theme
-- --------------------
-- Stores the theme config for an activity
--
-- Get theme by theme_id
-- > SELECT * FROM courseware.activity_theme WHERE id=?
--
CREATE TABLE IF NOT EXISTS courseware.activity_theme (
    id timeuuid,
    activity_id timeuuid,
    config text,
    PRIMARY KEY(id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
