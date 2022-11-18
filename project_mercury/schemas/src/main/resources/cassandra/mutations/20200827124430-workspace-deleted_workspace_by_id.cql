--
-- workspace.deleted_workspace_by_id
-- --------------------
-- Stores a list of deleted workspaces, so that they can be recovered if needed.
--
-- Find a workspace for a given id
-- > SELECT * FROM workspace.deleted_workspace_by_id WHERE workspace_id = ?
--
CREATE TABLE IF NOT EXISTS workspace.deleted_workspace_by_id (
    workspace_id timeuuid,
    account_id timeuuid,
    name text,
    deleted_at text,
    PRIMARY KEY ((workspace_id), deleted_at)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
