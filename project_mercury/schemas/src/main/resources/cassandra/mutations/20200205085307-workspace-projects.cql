--
-- workspace.project
-- -------------------
-- Stores projects information
--
-- Find a project by its id
-- > SELECT * FROM workspace.project WHERE id = ?
--
CREATE TABLE IF NOT EXISTS workspace.project (
    id timeuuid,
    workspace_id timeuuid,
    name text,
    config text,
    created_at text,
    PRIMARY KEY (id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
