--
-- workspace.account_by_project
-- ----------------------------
-- Store the accounts that have access to the project
--
-- Find all the accounts that have access to the project
-- > SELECT * FROM workspace.account_by_project WHERE project_id = ?
--
CREATE TABLE IF NOT EXISTS workspace.account_by_project (
    project_id timeuuid,
    account_id timeuuid,
    permission_level text,
    PRIMARY KEY (project_id, account_id)
) WITH CLUSTERING ORDER BY (account_id ASC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
