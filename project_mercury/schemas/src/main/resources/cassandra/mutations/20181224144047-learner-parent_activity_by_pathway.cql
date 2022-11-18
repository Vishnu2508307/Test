--
-- learner.parent_activity_by_pathway
-- --------------------
-- Store the parent relationship a deployed pathway has with its parent activity
--
-- Find the parent activity for the last deployed pathway
-- > SELECT * FROM learner.parent_activity_by_pathway WHERE pathway_id = ? AND deployment_id = ? LIMIT 1
--
CREATE TABLE IF NOT EXISTS learner.parent_activity_by_pathway (
    pathway_id timeuuid,
    deployment_id timeuuid,
    change_id timeuuid,
    activity_id timeuuid,
    PRIMARY KEY (pathway_id, deployment_id, change_id)
)
WITH CLUSTERING ORDER BY (deployment_id ASC, change_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
