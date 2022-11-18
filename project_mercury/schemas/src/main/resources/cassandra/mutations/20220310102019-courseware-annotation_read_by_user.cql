--
-- courseware.annotation_read_by_user
-- --------------------
-- Table to track comments that a user has read.
--
-- Write a description of the query that this table supports
-- > SELECT * FROM courseware.annotation_read_by_user WHERE annotation_id = ? and user_id = ?
--
CREATE TABLE IF NOT EXISTS courseware.annotation_read_by_user (
    annotion_id timeuuid,
    user_id timeuuid,
    primary key (annotion_id, user_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
