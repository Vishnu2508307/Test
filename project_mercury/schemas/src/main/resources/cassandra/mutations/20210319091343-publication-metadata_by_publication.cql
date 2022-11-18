--
-- publication.metadata_by_publication
-- ------------------------------------
-- This table store the metadata details by publication.
--
-- Find the metadata details for given publication id
-- > SELECT * FROM publication.metadata_by_publication WHERE publication_id=?
--
CREATE TABLE IF NOT EXISTS publication.metadata_by_publication (
    publication_id timeuuid,
    created_by timeuuid,
    created_at timeuuid,
    updated_at timeuuid,
    updated_by timeuuid,
    PRIMARY KEY (publication_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
