--
-- iam_global.fidm_ltiv11_launch_request_log
-- -----------------------------------------
-- Track the status of a launch request ordered by earliest first
--
-- Find the statuses of a launch request
-- > SELECT * FROM iam_global.fidm_ltiv11_launch_request_log WHERE launch_request_id = ?
--
CREATE TABLE IF NOT EXISTS iam_global.fidm_ltiv11_launch_request_log (
    launch_request_id timeuuid,
    id timeuuid,
    status text,
    message text,
    PRIMARY KEY(launch_request_id, id)
)
WITH CLUSTERING ORDER BY (id DESC)
 AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
