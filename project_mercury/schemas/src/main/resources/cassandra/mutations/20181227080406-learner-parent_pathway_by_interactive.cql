--
-- learner.parent_pathway_by_interactive
-- --------------------
-- Stores the parent relationship between a deployed interactive and its parent pathway
--
-- Find the parent pathway for the latest deployed interactive by id
-- > SELECT * FROM learner.parent_pathway_by_interactive WHERE interactive_id = ? AND deployment_id = ? LIMIT 1
--
CREATE TABLE IF NOT EXISTS learner.parent_pathway_by_interactive (
    interactive_id timeuuid,
    deployment_id timeuuid,
    change_id timeuuid,
    pathway_id timeuuid,
    PRIMARY KEY (interactive_id, deployment_id, change_id)
)
WITH CLUSTERING ORDER BY (deployment_id ASC, change_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
