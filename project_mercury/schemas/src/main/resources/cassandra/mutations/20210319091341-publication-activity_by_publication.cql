--
-- publication.activity_by_publication
-- ------------------------------------
-- This table store the activity details by publication.
--
-- Find the activity details for given publication id
-- > SELECT * FROM publication.activity_by_publication WHERE publication_id=?
--
CREATE TABLE IF NOT EXISTS publication.activity_by_publication (
    publication_id timeuuid,
    activity_id timeuuid,
    version text,
    title text,
    description text,
    PRIMARY KEY (publication_id, activity_id, version)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
