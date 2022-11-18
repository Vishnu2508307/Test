--
-- learner.parent_pathway_by_activity
-- --------------------
-- Store the parent relationship that a deployed activity has with a deployed pathway
--
-- Find the parent pathway for the by activity, deployment and latest change id
-- > SELECT * FROM learner.parent_pathway_by_activity WHERE activity_id = ? AND deployment_id = ? LIMIT 1
--
CREATE TABLE IF NOT EXISTS learner.parent_pathway_by_activity (
    activity_id timeuuid,
    deployment_id timeuuid,
    change_id timeuuid,
    pathway_id timeuuid,
    PRIMARY KEY (activity_id, deployment_id, change_id)
)
WITH CLUSTERING ORDER BY (deployment_id ASC, change_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
