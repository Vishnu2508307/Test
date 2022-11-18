--
-- workspace.account_by_workspace
-- -------------------------------
-- Stores the list accounts for a given workspace
--
-- Get list of accounts for a given workspace_id
-- > SELECT * FROM workspace.account_by_workspace WHERE workspace_id=?
--
CREATE TABLE IF NOT EXISTS workspace.account_by_workspace (
    workspace_id timeuuid,
    account_id timeuuid,
    PRIMARY KEY(workspace_id,account_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
