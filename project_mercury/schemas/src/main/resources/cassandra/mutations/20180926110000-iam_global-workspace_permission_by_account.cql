--
-- iam_global.workspace_permission_by_account
-- --------------------
-- Tracks permissions for workspaces
--
-- Find a permission level for an account for a workspace
-- > SELECT * FROM iam_global.workspace_permission_by_account WHERE account_id = ? and workspace_id = ?
--
CREATE TABLE IF NOT EXISTS iam_global.workspace_permission_by_account (
    account_id timeuuid,
    workspace_id timeuuid,
    permission_level text,
    PRIMARY KEY(account_id, workspace_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
