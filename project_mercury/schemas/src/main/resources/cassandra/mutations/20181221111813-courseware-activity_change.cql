--
-- courseware.activity_change
-- --------------------
-- Store the change id associated with the top level activity everytime the courseware structure changes
--
-- Select latest change id for a given activity
-- > SELECT * FROM courseware.activity_change WHERE activity_id = ? LIMIT 1
--
CREATE TABLE IF NOT EXISTS courseware.activity_change (
    activity_id timeuuid,
    change_id timeuuid,
    PRIMARY KEY (activity_id, change_id)
)
WITH CLUSTERING ORDER BY (change_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
