--
-- iam_global.claim_by_account
-- ---------------------------
-- Track the claims by account and subscription which created the claim
--
-- Find all the claims for a particular subscription
-- > SELECT * FROM iam_global.claim_by_account WHERE account_id = ? AND subscription_id = ?
--
CREATE TABLE IF NOT EXISTS iam_global.claim_by_account (
    account_id timeuuid,
    subscription_id timeuuid,
    name text,
    value text,
    PRIMARY KEY(account_id, subscription_id, name)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
