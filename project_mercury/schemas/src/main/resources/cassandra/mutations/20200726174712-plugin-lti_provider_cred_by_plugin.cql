--
-- plugin.lti_provider_cred_by_plugin
-- ----------------------------------
-- Store LTI Provider credentials (key/secret) by plugin id
--
-- Find all the keys for a plugin
-- > SELECT * FROM WHERE plugin_id = ?;
--
-- Find a specific key for a plugin
-- > SELECT * FROM WHERE plugin_id = ? AND lti_key = ?;
--
CREATE TABLE IF NOT EXISTS plugin.lti_provider_cred_by_plugin (
    plugin_id timeuuid,
    id timeuuid,
    lti_key text,
    lti_secret text,
    allowed_fields set<text>,
    PRIMARY KEY (plugin_id, lti_key)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
