--
-- subscription.team_by_subscription
-- --------------------
-- Tracks teams that have permissions over a subscription.
--
-- Find all teams that have permission over a subscription
-- > SELECT * FROM subscription.team_by_subscription WHERE subscription_id = ?
--
CREATE TABLE IF NOT EXISTS subscription.team_by_subscription (
    subscription_id timeuuid,
    team_id timeuuid,
    permission_level text,
    PRIMARY KEY (subscription_id, team_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
