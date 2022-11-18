--
-- iam_global.fidm_oidc_session_by_account
-- ---------------------------------------
-- Track OIDC sessions by account, newest first
--
-- Find all the OIDC sessions by account id
-- > SELECT * FROM iam_global.fidm_oidc_session_by_account WHERE account_id = ?
--
CREATE TABLE IF NOT EXISTS iam_global.fidm_oidc_session_by_account (
    account_id timeuuid,
    id timeuuid,
    session_id text,
    PRIMARY KEY(account_id, id)
)
WITH CLUSTERING ORDER BY (id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
