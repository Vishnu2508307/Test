--
-- courseware.deleted_activity_by_id
-- --------------------
-- Stores a list of deleted activities, so that they can be recovered if needed.
--
-- Find a activity for a given id
-- > SELECT * FROM courseware.deleted_activity_by_id WHERE activity_id = ?
--
CREATE TABLE IF NOT EXISTS courseware.deleted_activity_by_id (
    activity_id timeuuid,
    account_id timeuuid,
    deleted_at text,
    PRIMARY KEY ((activity_id), deleted_at)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
