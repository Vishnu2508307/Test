--
-- courseware.pathway
-- ------------------
-- Track pathway objects
--
-- Find a pathway by id
-- > SELECT * FROM courseware.pathway WHERE id = ?
--
CREATE TABLE IF NOT EXISTS courseware.pathway (
    id timeuuid,
    type text,
    activity_id timeuuid,
    PRIMARY KEY(id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
