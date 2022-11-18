--
-- courseware.icon_library_by_activity
-- --------------------
-- Save icon libraries for activity theme by an activity
--
-- Find all the icon libraries for activity theme by an activity
-- > SELECT * FROM courseware.icon_library_by_activity WHERE activity_id = ?
--
CREATE TABLE IF NOT EXISTS courseware.icon_library_by_activity (
    activity_id timeuuid,
    icon_library text,
    status text,
    PRIMARY KEY (activity_id, icon_library)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
