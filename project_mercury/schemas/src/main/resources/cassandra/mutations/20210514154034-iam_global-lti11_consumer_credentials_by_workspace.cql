--
-- iam_global.lti11_consumer_credentials_by_workspace
-- --------------------------------------------------
-- Store LTI credentials retrievable by workspace cohort and key
--
-- Find LTI credentials for a workspace and cohort by key
-- > SELECT * FROM iam_global.lti11_consumer_credentials_by_workspace WHERE workspace_id = ?
-- AND cohort_id = ?
-- AND oauth_consumer_key = ?
--
CREATE TABLE IF NOT EXISTS iam_global.lti11_consumer_credentials_by_workspace (
    workspace_id timeuuid,
    cohort_id timeuuid,
    oauth_consumer_key text,
    oauth_consumer_secret text,
    consumer_configuration_id timeuuid,
    id timeuuid,
    log_debug boolean,
    PRIMARY KEY (workspace_id, cohort_id, oauth_consumer_key)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
