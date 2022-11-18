--
-- iam_global.web_session_token
-- --------------------
-- Keeps web session tokens.
--
-- Find web session token
-- > SELECT * FROM iam_global.web_session_token WHERE key=?
--
CREATE TABLE IF NOT EXISTS iam_global.web_session_token (
    key text,
    account_id timeuuid,
    created_ts bigint,
    expired_ts bigint,
    PRIMARY KEY(key)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
