--
-- learner.child_pathway_by_activity
-- --------------------
-- Store the child relationship between a deployed activity and a list of deployed pathways
--
-- Find the children pathway ids for the latest change on a deployed activity
-- > SELECT * FROM learner.child_pathway_by_activity WHERE activity_id = ? AND deployment_id = ? LIMIT 1
--
CREATE TABLE IF NOT EXISTS learner.child_pathway_by_activity (
    activity_id timeuuid,
    deployment_id timeuuid,
    change_id timeuuid,
    pathway_ids list<timeuuid>,
    PRIMARY KEY (activity_id, deployment_id, change_id)
)
WITH CLUSTERING ORDER BY (deployment_id ASC, change_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
