--
-- learner.child_component_by_activity
-- --------------------
-- Store the child relationship between a deployed activity and multiple deployed components
--
-- Find all child components for the latest change of a deployed activity
-- > SELECT * FROM learner.child_component_by_activity WHERE activity_id = ? AND deployment_id = ? LIMIT 1
--
CREATE TABLE IF NOT EXISTS learner.child_component_by_activity (
    activity_id timeuuid,
    deployment_id timeuuid,
    change_id timeuuid,
    component_ids list<timeuuid>,
    PRIMARY KEY (activity_id, deployment_id, change_id)
)
WITH CLUSTERING ORDER BY (deployment_id ASC, change_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
