--
-- iam.project_permission_by_account
-- ---------------------------------
-- Store the permission for an account over projects
--
-- Find the permission level of an account over a project
-- > SELECT * FROM iam_global.project_permission_by_account WHERE account_id = ? AND project_id = ?
--
CREATE TABLE IF NOT EXISTS iam_global.project_permission_by_account (
    account_id timeuuid,
    project_id timeuuid,
    permission_level text,
    PRIMARY KEY (account_id, project_id)
) WITH CLUSTERING ORDER BY (project_id ASC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
