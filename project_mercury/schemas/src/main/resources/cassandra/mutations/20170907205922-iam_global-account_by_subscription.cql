--
-- iam_global.account_by_subscription
-- ----------------------------------
-- Track all accounts which are on a particular subscription.
--
-- Find all the accounts for a particular subscription id.
-- > SELECT * FROM WHERE subscription_id = ?;
--
-- Find an account on a particular subscription id.
-- > SELECT * FROM WHERE subscription_id = ? AND account_id = ?;
--
CREATE TABLE IF NOT EXISTS iam_global.account_by_subscription (
    subscription_id timeuuid,
    account_id timeuuid,
    PRIMARY KEY (subscription_id, account_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
