--
-- analytics_event.event_summary
-- -----------------------------
-- This table stores the attributes of the event that will be published.
--
-- Get event summary by id
-- > SELECT * FROM analytics_event.event_summary WHERE id = ?;
--
CREATE TABLE IF NOT EXISTS analytics_event.event_summary (
    id timeuuid,
    namespace text,
    message_type_code text,
    version text,
    stream_type text,
    create_type text,
    correlation_id text,
    payload text,
    tags map<text, text>,
    PRIMARY KEY (id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
