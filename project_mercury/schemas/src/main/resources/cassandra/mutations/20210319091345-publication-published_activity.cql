--
-- publication.published_activity
-- -------------------------------
-- This table store the published activity details.
--
-- Find the published activity details for given activity id
-- > SELECT * FROM publication.published_activity WHERE activity_id=?
--
CREATE TABLE IF NOT EXISTS publication.published_activity (
    activity_id timeuuid,
    version text,
    publication_id timeuuid,
    title text,
    description text,
    PRIMARY KEY (activity_id, version)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
