--
-- asset.alfresco_sync_summary_by_course
-- -------------------------
-- This table store the alfresco sync summary details by course
--
--  Find the alfresco sync summary details for given course id
-- > SELECT * FROM asset.alfresco_sync_summary_by_course WHERE course_id=?
--
CREATE TABLE IF NOT EXISTS asset.alfresco_sync_summary_by_course (
    course_id timeuuid,
    reference_id timeuuid,
    sync_type text,
    status text,
    completed_at timeuuid,
    primary key (course_id, reference_id)
) WITH CLUSTERING ORDER BY (reference_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
