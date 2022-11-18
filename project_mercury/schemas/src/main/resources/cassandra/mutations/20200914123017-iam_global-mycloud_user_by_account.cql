--
-- iam_global.mycloud_user_by_account
-- ------------------------------
-- Track an account if in bronte for a myCloud user id
--
-- Find the myCloud user id for an account id
-- > SELECT * FROM iam_global.mycloud_user_by_account WHERE account_id = ?
--
CREATE TABLE IF NOT EXISTS iam_global.mycloud_user_by_account (
    account_id timeuuid,
    mycloud_user_id text,
    PRIMARY KEY (account_id, mycloud_user_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
