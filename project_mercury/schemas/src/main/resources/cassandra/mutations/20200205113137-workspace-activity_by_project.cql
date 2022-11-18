--
-- workspace.activity_by_project
-- -----------------------------
-- Stores all the activities that exist in a project
--
-- Find all the activities that belong to a project
-- > SELECT * FROM workspace.activity_by_project WHERE project_id = ?
--
CREATE TABLE IF NOT EXISTS workspace.activity_by_project (
    project_id timeuuid,
    activity_id timeuuid,
    PRIMARY KEY (project_id, activity_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
