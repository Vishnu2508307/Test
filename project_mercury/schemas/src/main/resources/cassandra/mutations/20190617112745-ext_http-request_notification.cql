--
-- ext_http.request_notification
-- -----------------------------
-- Track submitted requests to the External HTTP Service
--
-- Find a request by id
-- > SELECT * FROM ext_http.request_notification WHERE notification_id = ?
--
CREATE TABLE IF NOT EXISTS ext_http.request_notification (
    notification_id timeuuid,
    reference_id timeuuid,
    purpose text,
    params text,
    PRIMARY KEY (notification_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
