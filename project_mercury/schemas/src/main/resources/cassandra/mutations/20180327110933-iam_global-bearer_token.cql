--
-- iam_global.bearer_token
-- --------------------
-- Keeps active tokens for user authorization.
-- Tokens are stored with time to live (TTL) based on their type.
--
-- Find a specific token
-- > SELECT * FROM iam_global.bearer_token WHERE key = ?
--
CREATE TABLE IF NOT EXISTS iam_global.bearer_token (
    key text,
    account_id timeuuid,
    PRIMARY KEY(key)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
