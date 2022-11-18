--
-- workspace.workspace_by_subscription
-- -----------------------------------
-- Track workspaces for each subscription
--
-- Find all the workspaces for a given subscription
-- > SELECT * FROM workspace.workspace_by_subscription WHERE subscription_id = ?
--
CREATE TABLE IF NOT EXISTS workspace.workspace_by_subscription (
    subscription_id timeuuid,
    workspace_id timeuuid,
    PRIMARY KEY(subscription_id, workspace_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
