--
-- ext_http.request_log
-- --------------------
-- Track the status of submitted http requests, ordered from newest to oldest
--
-- Find the log history of a submitted request
-- > SELECT * FROM ext_http.request_log WHERE notification_id = ?
--
CREATE TABLE IF NOT EXISTS ext_http.request_log (
    id timeuuid,
    notification_id timeuuid,
    event text,
    PRIMARY KEY (notification_id, id)
)
WITH CLUSTERING ORDER BY (id DESC)
 AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
