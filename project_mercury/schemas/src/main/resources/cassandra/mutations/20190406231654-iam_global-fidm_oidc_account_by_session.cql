--
-- iam_global.fidm_oidc_account_by_session
-- ---------------------------------------
-- Track accounts by OIDC sessions
--
-- Find the account id for the OIDC session
-- > SELECT * FROM iam_global.fidm_oidc_account_by_session WHERE account_id = ?
--
CREATE TABLE IF NOT EXISTS iam_global.fidm_oidc_account_by_session (
    session_id text,
    account_id timeuuid,
    PRIMARY KEY(session_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
