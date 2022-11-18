--
-- iam_global.lti11_consumer_credentials
-- -------------------------------------
-- Store LTI credentials retrievable by id.
--
-- Find LTI credentials by id
-- > SELECT * FROM iam_global.lti11_consumer_credentials WHERE id = ?
--
CREATE TABLE IF NOT EXISTS iam_global.lti11_consumer_credentials (
    id timeuuid,
    oauth_consumer_key text,
    oauth_consumer_secret text,
    cohort_id timeuuid,
    workspace_id timeuuid,
    consumer_configuration_id timeuuid,
    log_debug boolean,
    PRIMARY KEY (id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
