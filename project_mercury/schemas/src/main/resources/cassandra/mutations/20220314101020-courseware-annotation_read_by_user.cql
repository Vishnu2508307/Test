--
-- courseware.annotation_read_by_user
-- --------------------
-- Table to track comments that a user have read.
--
-- Write a description of the query that this table supports
-- > SELECT * FROM courseware.annotation_read_by_user WHERE root_element_id = ? and element_id = ? and annotation_id = ? and user_id = ?
--
CREATE TABLE IF NOT EXISTS courseware.annotation_read_by_user (
    root_element_id timeuuid,
    element_id timeuuid,
    annotation_id timeuuid,
    user_id timeuuid,
    primary key ((root_element_id), element_id, annotation_id, user_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
