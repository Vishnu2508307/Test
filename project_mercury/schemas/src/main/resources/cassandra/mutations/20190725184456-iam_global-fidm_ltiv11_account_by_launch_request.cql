--
-- iam_global.fidm_ltiv11_account_by_launch_request
-- ------------------------------------------------
-- Track the account for this launch request
--
-- Find the account for the provided launch request id
-- > SELECT * FROM iam_global.fidm_ltiv11_account_by_launch_request WHERE launch_request_id = ?
--
CREATE TABLE IF NOT EXISTS iam_global.fidm_ltiv11_account_by_launch_request (
    launch_request_id timeuuid,
    account_id timeuuid,
    PRIMARY KEY (launch_request_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
