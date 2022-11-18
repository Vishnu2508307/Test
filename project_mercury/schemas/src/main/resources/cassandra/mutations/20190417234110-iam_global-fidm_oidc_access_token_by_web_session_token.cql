--
-- iam_global.fidm_oidc_access_token_by_web_session_token
-- ------------------------------------------------------
-- Track the OIDC access_token and state by the web session token generated
--
-- Find the OIDC access_token by web session or bearer token
-- > SELECT * FROM iam_global.fidm_oidc_access_token_by_web_session_token WHERE web_session_token = ?
--
CREATE TABLE IF NOT EXISTS iam_global.fidm_oidc_access_token_by_web_session_token (
    web_session_token text,
    id timeuuid,
    state text,
    relying_party_id timeuuid,
    access_token text,
    token_type text,
    expires_in bigint,
    PRIMARY KEY (web_session_token)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
