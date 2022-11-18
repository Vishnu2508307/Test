--
-- iam_global.developer_key
-- --------------------
-- Holds the developer key
--
-- Find a developer key by value
-- > SELECT * FROM iam_global.developer_key WHERE key = ?.
--
CREATE TABLE IF NOT EXISTS iam_global.developer_key (
    key text,
    subscription_id timeuuid,
    account_id timeuuid,
    created_ts bigint,
    PRIMARY KEY (key)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
