--
-- learner.deployment_by_activity
-- --------------------
-- Store the deployment of an activity along with the change id
--
-- Find all deployments for an activity
-- > SELECT * FROM learner.deployment_by_activity WHERE activity_id = ?
--
CREATE TABLE IF NOT EXISTS learner.deployment_by_activity (
    activity_id timeuuid,
    deployment_id timeuuid,
    change_id timeuuid,
    PRIMARY KEY (activity_id, deployment_id, change_id)
)
WITH CLUSTERING ORDER BY (deployment_id ASC, change_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
