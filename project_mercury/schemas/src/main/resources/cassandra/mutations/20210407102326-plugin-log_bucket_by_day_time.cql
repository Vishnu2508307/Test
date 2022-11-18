--
-- plugin.log_bucket_by_day_time
-- -----------------------------
-- This table stores bucket_id that belongs to a table_name in that day/time.
--
-- Find all the bucket ids by day time for a table
-- > SELECT * FROM plugin.log_bucket_by_day_time WHERE day = ? AND time = ?;
--
CREATE TABLE IF NOT EXISTS plugin.log_bucket_by_day_time (
    day date,
    time time,
    table_name text,
    bucket_id timeuuid,
    PRIMARY KEY((day, time), table_name, bucket_id)
)
WITH CLUSTERING ORDER BY (table_name DESC, bucket_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
