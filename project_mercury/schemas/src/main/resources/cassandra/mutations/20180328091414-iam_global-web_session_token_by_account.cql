--
-- iam_global.web_session_token_by_account
-- --------------------
-- Keeps web session tokens for account
--
-- Find all web session tokens for account
-- > SELECT * FROM iam_global.web_session_token_by_account WHERE account_id=?
--
CREATE TABLE IF NOT EXISTS iam_global.web_session_token_by_account (
    account_id timeuuid,
    key text,
    created_ts bigint,
    expired_ts bigint,
    PRIMARY KEY(account_id, key)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
