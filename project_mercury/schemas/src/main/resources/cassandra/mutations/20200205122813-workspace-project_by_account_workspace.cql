--
-- workspace.project_by_account
-- ----------------------------
-- Store all the projects an account has access to
--
-- Find all the project an account has access to within a workspace
-- > SELECT * FROM workspace.project_by_account_workspace WHERE account_id = ? AND workspace_id = ?
--
CREATE TABLE IF NOT EXISTS workspace.project_by_account_workspace (
    account_id timeuuid,
    workspace_id timeuuid,
    project_id timeuuid,
    PRIMARY KEY ((account_id, workspace_id), project_id)
) WITH CLUSTERING ORDER BY (project_id ASC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
