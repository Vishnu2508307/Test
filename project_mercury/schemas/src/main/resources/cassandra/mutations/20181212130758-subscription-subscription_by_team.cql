--
-- subscription.subscription_by_team
-- --------------------
-- Tracks subscriptions that have been shared with a team
--
-- Find all subscriptions a team has permission level over
-- > SELECT * FROM subscription.subscription_by_team WHERE team_id = ?
--
CREATE TABLE IF NOT EXISTS subscription.subscription_by_team (
    team_id timeuuid,
    subscription_id timeuuid,
    PRIMARY KEY (team_id, subscription_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
