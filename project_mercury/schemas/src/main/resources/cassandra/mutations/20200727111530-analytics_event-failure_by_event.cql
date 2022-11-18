--
-- analytics_event.failure_by_event
-- --------------------------------
-- Track all event publishing failures
--
-- Get all failures for an event
-- > SELECT * FROM analytics_event.failure_by_event WHERE event_id =?;
--
-- Get a failure for an event given an event_id and fail_id
-- > SELECT * FROM analytics_event.failure_by_event WHERE event_id =? and fail_id = ?;
--
CREATE TABLE IF NOT EXISTS analytics_event.failure_by_event (
    event_id timeuuid,
    fail_id timeuuid,
    exception_message text,
    PRIMARY KEY (event_id, fail_id)
) WITH CLUSTERING ORDER BY (fail_id DESC) AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
