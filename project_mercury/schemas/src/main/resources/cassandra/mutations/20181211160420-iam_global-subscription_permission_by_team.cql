--
-- iam_global.subscription_permission_by_team
-- --------------------
-- Tracks subscription permissions by team
--
-- Find a permission level for a team over a subscription
-- > SELECT * FROM iam_global.subscription_permission_by_team WHERE team_id = ? AND subscription_id = ?
--
CREATE TABLE IF NOT EXISTS iam_global.subscription_permission_by_team (
    team_id timeuuid,
    subscription_id timeuuid,
    permission_level text,
    PRIMARY KEY (team_id, subscription_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
