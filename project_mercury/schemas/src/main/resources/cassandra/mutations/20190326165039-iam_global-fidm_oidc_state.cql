--
-- iam_global.fidm_oidc_state
-- --------------------------
-- Keeps parameters between OpenID Connect requests by state
--
-- Find parameters by state value
-- > SELECT * FROM iam_global.fidm_oidc_state WHERE state = ?
--
CREATE TABLE IF NOT EXISTS iam_global.fidm_oidc_state (
    state text,
    redirect_url text,
    relying_party_id timeuuid,
    nonce text,
    PRIMARY KEY (state)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
