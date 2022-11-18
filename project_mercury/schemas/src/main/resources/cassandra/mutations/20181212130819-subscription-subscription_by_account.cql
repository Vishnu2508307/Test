--
-- subscription.subscription_by_account
-- --------------------
-- Tracks subscriptions that have been shared with an account
--
-- Find all subscriptions an account has permission level over
-- > SELECT * FROM subscription.subscription_by_account WHERE account_id = ?
--
CREATE TABLE IF NOT EXISTS subscription.subscription_by_account (
    account_id timeuuid,
    subscription_id timeuuid,
    PRIMARY KEY (account_id, subscription_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
