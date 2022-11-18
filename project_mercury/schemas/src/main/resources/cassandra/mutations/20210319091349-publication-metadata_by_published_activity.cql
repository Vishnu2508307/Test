--
-- publication.metadata_by_published_activity
-- -------------------------------------------
-- This table store the metadata details by published activity.
--
-- Find the metadata details for given activity id
-- > SELECT * FROM publication.metadata_by_published_activity WHERE activity_id=?
--
CREATE TABLE IF NOT EXISTS publication.metadata_by_published_activity (
    activity_id timeuuid,
    version text,
    publication_id timeuuid,
    created_at timeuuid,
    created_by timeuuid,
    updated_at timeuuid,
    updated_by timeuuid,
    primary key (activity_id, version)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
