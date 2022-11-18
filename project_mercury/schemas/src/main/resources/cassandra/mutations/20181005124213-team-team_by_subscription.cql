--
-- team.team_by_subscription
-- -------------------------
-- This table stores information of the teams that belong to a subscription
--
-- Get all teams for a subscription
-- > SELECT * FROM team.team_by_subscription WHERE subscription_id = ?;
--
CREATE TABLE IF NOT EXISTS team.team_by_subscription (
    subscription_id timeuuid,
    team_id timeuuid,
    PRIMARY KEY(subscription_id, team_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
