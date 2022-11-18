--
-- iam_global.developer_key_by_account
-- --------------------
-- Tracks a developer key by a user account.
--
-- Finds a developer key by user account
-- > SELECT * FROM iam_global.developer_key_by_account WHERE account_id = ?
--
CREATE TABLE IF NOT EXISTS iam_global.developer_key_by_account (
    account_id timeuuid,
    key text,
    subscription_id timeuuid,
    created_ts bigint,
    PRIMARY KEY (account_id, key)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
