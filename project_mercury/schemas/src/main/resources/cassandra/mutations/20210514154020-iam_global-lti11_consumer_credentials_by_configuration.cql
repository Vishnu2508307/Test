--
-- iam_global.lti11_consumer_credentials_by_configuration
-- ------------------------------------------------------
-- Store LTI credentials retrievable by consumer configuration id
--
-- Find all the LTI credentials for a consumer configuration
-- > SELECT * FROM iam_global.lti11_consumer_credentials_by_configuration WHERE consumer_configuration_id = ?
--
CREATE TABLE IF NOT EXISTS iam_global.lti11_consumer_credentials_by_configuration (
    consumer_configuration_id timeuuid,
    id timeuuid,
    oauth_consumer_key text,
    oauth_consumer_secret text,
    workspace_id timeuuid,
    cohort_id timeuuid,
    log_debug boolean,
    PRIMARY KEY (consumer_configuration_id, id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
