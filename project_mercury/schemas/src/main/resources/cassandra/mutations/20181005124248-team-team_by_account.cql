--
-- team.team_by_account
-- --------------------
-- This is a mapping table of the account to various teams
--
-- Get the list of a teams that an account is part of.
-- > SELECT * FROM team.team_by_account WHERE account_id = ?;
--
CREATE TABLE IF NOT EXISTS team.team_by_account (
    account_id timeuuid,
    team_id timeuuid,
    PRIMARY KEY (account_id,team_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
