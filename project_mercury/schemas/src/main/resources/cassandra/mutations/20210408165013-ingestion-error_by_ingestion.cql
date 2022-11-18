--
-- ingestion.error_by_ingestion
-- --------------------
-- Store errors that can be queried given an ingestion id
--
-- Find all errors for an ingestion
-- > SELECT * FROM ingestion.error_by_ingestion WHERE ingestion_id = ?;
--
CREATE TABLE IF NOT EXISTS ingestion.error_by_ingestion (
    ingestion_id timeuuid,
    cause text,
    error_message text,
    PRIMARY KEY (ingestion_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
