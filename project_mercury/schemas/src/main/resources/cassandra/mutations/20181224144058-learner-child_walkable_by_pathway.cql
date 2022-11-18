--
-- learner.child_walkable_by_pathway
-- --------------------
-- Store the relationship between a deployed pathway and its deployed walkable children
--
-- Find all walkable children for the latest deployed pathway
-- > SELECT * FROM learner.child_walkable_by_pathway WHERE pathway_id = ? AND deployment_id = ? LIMIT 1
--
CREATE TABLE IF NOT EXISTS learner.child_walkable_by_pathway (
    pathway_id timeuuid,
    deployment_id timeuuid,
    change_id timeuuid,
    walkable_ids list<timeuuid>,
    walkable_types map<timeuuid, text>,
    PRIMARY KEY (pathway_id, deployment_id, change_id)
)
WITH CLUSTERING ORDER BY (deployment_id ASC, change_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
