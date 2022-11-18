--
-- iam_global.fidm_oidc_rp_credential
-- ----------------------------------
-- Keeps credentials for SSO OpenID Connect providers
--
-- Find credentials for relying party
-- > SELECT * FROM iam_global.fidm_oidc_rp_credential WHERE relying_party_id = ?
--
CREATE TABLE IF NOT EXISTS iam_global.fidm_oidc_rp_credential (
    relying_party_id timeuuid,
    subscription_id timeuuid,
    issuer_url text,
    client_id text,
    client_secret text,
    authentication_request_scope text,
    log_debug boolean,
    enforce_verified_email boolean,
    PRIMARY KEY (relying_party_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
