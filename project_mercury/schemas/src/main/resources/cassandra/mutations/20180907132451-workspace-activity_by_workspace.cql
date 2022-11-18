--
-- workspace.activity_by_workspace
-- --------------------
-- Track activities by workspace: which activities should belong and be shown for particular workspace.
--
-- Select activities for given workspace
-- > SELECT * FROM workspace.activity_by_workspace WHERE workspace_id = ?
--
CREATE TABLE IF NOT EXISTS workspace.activity_by_workspace (
    workspace_id timeuuid,
    activity_id timeuuid,
    PRIMARY KEY (workspace_id, activity_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
