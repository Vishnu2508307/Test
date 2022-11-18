--
-- ingestion.event_by_ingestion
-- --------------------
-- Tracks all ingestion events by ingestion id
--
-- Fetch events by ingestion id
-- > SELECT * FROM ingestion.event_by_ingestion WHERE ingestion_id = ?;
--
CREATE TABLE IF NOT EXISTS ingestion.event_by_ingestion (
    ingestion_id timeuuid,
    event_id timeuuid,
    project_id timeuuid,
    event_type text,
    code text,
    message text,
    error text,
    action text,
    location text,
    PRIMARY KEY (ingestion_id, event_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
