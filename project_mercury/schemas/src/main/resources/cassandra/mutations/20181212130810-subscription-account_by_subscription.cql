--
-- subscription.account_by_subscription
-- --------------------
-- Keeps track of the permission level that multiple accounts have over a subscription entity
--
-- Find all accounts that have permission over a subscription
-- > SELECT * FROM subscription.account_by_subscription WHERE subscription_id = ?
--
CREATE TABLE IF NOT EXISTS subscription.account_by_subscription (
    subscription_id timeuuid,
    account_id timeuuid,
    permission_level text,
    PRIMARY KEY (subscription_id, account_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
