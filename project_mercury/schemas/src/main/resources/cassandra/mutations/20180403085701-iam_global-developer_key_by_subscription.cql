--
-- iam_global.developer_key_by_subscription
-- --------------------
-- Holds the developer key tracked by subscription_id.
--
-- Find all key by subscription
-- > SELECT * FROM iam_global.developer_key_by_subscription WHERE subscription_id = ?.
--
CREATE TABLE IF NOT EXISTS iam_global.developer_key_by_subscription (
    subscription_id timeuuid,
    key text,
    account_id timeuuid,
    created_ts bigint,
    PRIMARY KEY (subscription_id, account_id, key)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
