--
-- iam_global.subscription_permission_by_account
-- --------------------
-- Keeps track of the permission level that an account has over a subscription entity
--
-- Find all the subscription permission by account_id
-- > SELECT * FROM iam_global.subscription_permission_by_account WHERE account_id = ?
--
CREATE TABLE IF NOT EXISTS iam_global.subscription_permission_by_account (
    account_id timeuuid,
    subscription_id timeuuid,
    permission_level text,
    PRIMARY KEY(account_id, subscription_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
