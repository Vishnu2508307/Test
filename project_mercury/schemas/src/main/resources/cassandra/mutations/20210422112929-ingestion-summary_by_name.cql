--
-- ingestion.summary_by_name
-- --------------------
-- Tracks all ingested data by course name
--
-- Fetch ingestion summary by name and project id
-- > SELECT * FROM ingestion.summary_by_name WHERE course_name = ? and project_id = ?;
--
CREATE TABLE IF NOT EXISTS ingestion.summary_by_name (
    ingestion_id timeuuid,
    project_id timeuuid,
    course_name text,
    config_fields text,
    creator_id timeuuid,
    ambrosia_url text,
    status text,
    ingestion_stats text,
    primary key (course_name, project_id, ingestion_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
