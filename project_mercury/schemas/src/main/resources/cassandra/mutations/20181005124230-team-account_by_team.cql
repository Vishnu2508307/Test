--
-- team.account_by_team
-- --------------------
-- This table stores information of the accounts with associated permission level that belong to a team
--
-- Get all accounts for a team
-- > SELECT * FROM team.account_by_team WHERE team_id = ?;
--
CREATE TABLE IF NOT EXISTS team.account_by_team (
    team_id timeuuid,
    account_id timeuuid,
    permission_level text,
    PRIMARY KEY (team_id,account_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
