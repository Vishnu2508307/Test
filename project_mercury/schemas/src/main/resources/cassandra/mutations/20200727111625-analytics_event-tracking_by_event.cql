--
-- analytics_event.tracking_by_event
-- ---------------------------------
-- Track all successfully published events.
--
-- Get tracking_id for a given event
-- > SELECT * FROM analytics_event.tracking_by_event WHERE event_id = ?;
--
CREATE TABLE IF NOT EXISTS analytics_event.tracking_by_event (
    event_id timeuuid,
    tracking_id uuid,
    PRIMARY KEY (event_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
