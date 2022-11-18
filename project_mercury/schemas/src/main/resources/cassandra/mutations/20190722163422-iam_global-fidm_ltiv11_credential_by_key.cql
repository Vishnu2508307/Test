--
-- iam_global.fidm_ltiv11_credential_by_key
-- ----------------------------------------
-- Track credentials for LTI v1.1 by key.
--
-- Find a consumer credential by oauth consumer key
-- > SELECT * FROM iam_global.fidm_ltiv11_credential_by_key WHERE oauth_consumer_key = ?
--
CREATE TABLE IF NOT EXISTS iam_global.fidm_ltiv11_credential_by_key (
    oauth_consumer_key text,
    id timeuuid,
    oauth_consumer_secret text,
    subscription_id timeuuid,
    comment text,
    PRIMARY KEY (oauth_consumer_key)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
