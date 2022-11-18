--
-- iam_global.account_by_mycloud_user
-- ------------------------------
-- Track an myCloud user id to its bronte account id
--
-- Find the account id for an myCloud user
-- > SELECT * FROM iam_global.account_by_mycloud_user WHERE mycloud_user_id = ?
--
CREATE TABLE IF NOT EXISTS iam_global.account_by_mycloud_user (
    mycloud_user_id text,
    account_id timeuuid,
    PRIMARY KEY (mycloud_user_id, account_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
