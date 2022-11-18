--
-- iam_global.fidm_ltiv11_credential
-- ---------------------------------
-- Track credentials for LTI v1.1 by id.
--
-- Find a consumer credential by id
-- > SELECT * FROM iam_global.fidm_ltiv11_credential WHERE id = ?
--
CREATE TABLE IF NOT EXISTS iam_global.fidm_ltiv11_credential (
    id timeuuid,
    oauth_consumer_key text,
    oauth_consumer_secret text,
    subscription_id timeuuid,
    comment text,
    PRIMARY KEY (id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
