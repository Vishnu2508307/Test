--
-- ingestion.summary_by_project
-- --------------------
-- Tracks the list of ingested items by project
--
-- Fetch all ingestion summary by project id
-- > SELECT * FROM ingestion.summary_by_project WHERE project_id = ?;
--
CREATE TABLE IF NOT EXISTS ingestion.summary_by_project (
    ingestion_id timeuuid,
    project_id timeuuid,
    config_fields text,
    creator_id timeuuid,
    ambrosia_url text,
    status text,
    ingestion_stats text,
    PRIMARY KEY (project_id, ingestion_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
