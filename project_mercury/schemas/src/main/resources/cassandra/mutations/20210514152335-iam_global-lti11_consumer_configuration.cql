--
-- iam_global.lti11_consumer_configuration
-- ---------------------------------------
-- Store the LTI configuration id global to a workspace
--
-- Find an LTI global configuration by id
-- > SELECT * FROM iam_global.lti11_consumer_configuration WHERE id = ?
--
CREATE TABLE IF NOT EXISTS iam_global.lti11_consumer_configuration (
    id timeuuid,
    workspace_id timeuuid,
    comment text,
    PRIMARY KEY (id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
