--
-- ingestion.event
-- --------------------
-- Tracks all events that occur during the ingestion process
--
-- Fetch event by unique event id
-- > SELECT * FROM ingestion.event WHERE id = ?;
--
CREATE TABLE IF NOT EXISTS ingestion.event (
    id timeuuid,
    ingestion_id timeuuid,
    project_id timeuuid,
    event_type text,
    code text,
    message text,
    error text,
    action text,
    location text,
    PRIMARY KEY (id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
