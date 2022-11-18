--
-- workspace.workspace_by_account
-- -------------------------------
-- Stores the workspaces for a given account
--
-- Get list of workspaces for a given account_id
-- > SELECT * FROM workspace.workspace_by_account WHERE account_id=?
--
CREATE TABLE IF NOT EXISTS workspace.workspace_by_account (
    account_id timeuuid,
    workspace_id timeuuid,
    PRIMARY KEY(account_id, workspace_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
