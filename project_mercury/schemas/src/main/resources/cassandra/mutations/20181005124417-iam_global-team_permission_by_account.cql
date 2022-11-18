--
-- iam_global.team_permission_by_account
-- -------------------------------------
-- This table is used for authorization of the permissions for an account belonging to a team.
--
-- Get the teams and permission levels for an account
-- > SELECT * FROM iam_global.team_permission_by_account WHERE account_id = ?;
-- Get the permission level for an account and a team
-- > SELECT * FROM iam_global.team_permission_by_account WHERE account_id = ? and team_id = ?;
--
CREATE TABLE IF NOT EXISTS iam_global.team_permission_by_account (
    account_id timeuuid,
    team_id timeuuid,
    permission_level text,
    PRIMARY KEY(account_id, team_id)
)
WITH CLUSTERING ORDER BY (team_id ASC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
