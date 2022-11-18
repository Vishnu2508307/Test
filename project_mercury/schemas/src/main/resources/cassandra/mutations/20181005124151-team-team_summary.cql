--
-- team.team_summary
-- -----------------
-- This table contains the summary for teams.
--
-- Get team summary for a team by id
-- > SELECT * FROM team.team_summary WHERE id = ?;
--
CREATE TABLE IF NOT EXISTS team.team_summary (
    id timeuuid,
    subscription_id timeuuid,
    name text,
    description text,
    thumbnail text,
    PRIMARY KEY(id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
