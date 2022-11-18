--
-- publication.publication_by_activity
-- ------------------------------------
-- This table store the publication by activity.
--
-- Find the publication for given activity id
-- > SELECT * FROM publication.publication_by_activity WHERE activity_id=?
--
CREATE TABLE IF NOT EXISTS publication.publication_by_activity (
    activity_id timeuuid,
    publication_id timeuuid,
    PRIMARY KEY (activity_id, publication_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
