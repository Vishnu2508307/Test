--
-- workspace.project_by_activity
-- -----------------------------
-- Stores the parent project id for an activity
--
-- Find the project id an activity belongs to
-- > SELECT * FROM workspace.project_by_activity WHERE activity_id = ?
--
CREATE TABLE IF NOT EXISTS workspace.project_by_activity (
    activity_id timeuuid,
    project_id timeuuid,
    PRIMARY KEY (activity_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
