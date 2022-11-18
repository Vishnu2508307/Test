--
-- iam_global.fidm_ltiv11_launch_request_by_account
-- ------------------------------------------------
-- Track launch requests by account, ordered by most recent first
--
-- Find all the request for a given account
-- > SELECT * FROM iam_global.fidm_ltiv11_launch_request_by_account WHERE account_id = ?
--
CREATE TABLE IF NOT EXISTS iam_global.fidm_ltiv11_launch_request_by_account (
    account_id timeuuid,
    launch_request_id timeuuid,
    PRIMARY KEY (account_id, launch_request_id)
)
WITH CLUSTERING ORDER BY (launch_request_id DESC)
 AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
