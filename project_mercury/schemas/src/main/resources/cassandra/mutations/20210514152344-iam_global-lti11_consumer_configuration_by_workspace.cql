--
-- iam_global.lti11_consumer_configuration_by_workspace
-- ----------------------------------------------------
-- Store the LTI consumer configuration id agains a workspace (1 to 1)
--
-- Find the LTI configuration id for a workspace
-- > SELECT * FROM iam_global.lti11_consumer_configuration_by_workspace WHERE workspace_id = ?
--
CREATE TABLE IF NOT EXISTS iam_global.lti11_consumer_configuration_by_workspace (
    workspace_id timeuuid,
    id timeuuid,
    comment text,
    PRIMARY KEY (workspace_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
