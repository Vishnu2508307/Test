--
-- learner.child_component_by_interactive
-- --------------------
-- Stores the child relationship between a deployed interactive and any chil component
--
-- Find a child component for the latest change of a deployed interactive
-- > SELECT * FROM learner.child_component_by_interactive WHERE interactive_id = ? AND deployment_id = ? LIMIT 1
--
CREATE TABLE IF NOT EXISTS learner.child_component_by_interactive (
    interactive_id timeuuid,
    deployment_id timeuuid,
    change_id timeuuid,
    component_ids list<timeuuid>,
    PRIMARY KEY (interactive_id, deployment_id, change_id)
)
WITH CLUSTERING ORDER BY (deployment_id ASC, change_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
