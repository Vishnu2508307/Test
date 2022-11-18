--
-- iam_global.claim_name_by_subscription
-- -------------------------------------
-- Track the names that are tracked across a subscription
--
-- Find all the names which have been provided by a subscription
-- > SELECT * FROM iam_global.claim_name_by_subscription WHERE subscription_id = ?
--
CREATE TABLE IF NOT EXISTS iam_global.claim_name_by_subscription (
    subscription_id timeuuid,
    name text,
    PRIMARY KEY(subscription_id, name)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
