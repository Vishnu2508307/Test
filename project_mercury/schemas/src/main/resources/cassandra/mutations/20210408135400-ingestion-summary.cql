--
-- ingestion.summary
-- --------------------
-- Tracks all ingested data.
--
-- Fetch ingestion summary by id
-- > SELECT * FROM ingestion.summary WHERE id= ?;
--
CREATE TABLE IF NOT EXISTS ingestion.summary (
    id timeuuid,
    project_id timeuuid,
    config_fields text,
    creator_id timeuuid,
    ambrosia_url text,
    status text,
    ingestion_stats text,
    primary key (id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
