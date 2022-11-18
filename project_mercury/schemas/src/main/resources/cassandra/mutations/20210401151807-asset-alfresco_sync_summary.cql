--
-- asset.alfresco_sync_summary
-- --------------------
-- This table store the alfresco sync summary details
--
-- Find the alfresco sync summary details for given reference id
-- > SELECT * FROM asset.alfresco_sync_summary WHERE reference_id=?
--
CREATE TABLE IF NOT EXISTS asset.alfresco_sync_summary (
    reference_id timeuuid,
    course_id timeuuid,
    sync_type text,
    status text,
    completed_at timeuuid,
    primary key (reference_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
