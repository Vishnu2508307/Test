--
-- learner.scenario_by_parent
-- --------------------
-- Stores the parent relationship between a deployed scenario and its parent element
--
-- Find scenarios by its parent
-- > SELECT * FROM learner.scenario_by_parent WHERE parent_id = ? AND deployment_id = ? LIMIT 1
--
-- Find all specific lifecycle scenarios by its parent
-- > SELECT * FROM learner.scenario_by_parent WHERE parent_id = ? AND deployment_id = ? AND change_id = ? AND lifecycle = ?
--
CREATE TABLE IF NOT EXISTS learner.scenario_by_parent (
    parent_id timeuuid,
    deployment_id timeuuid,
    change_id timeuuid,
    lifecycle text,
    parent_type text,
    scenario_ids list<timeuuid>,
    PRIMARY KEY (parent_id, deployment_id, change_id, lifecycle)
)
WITH CLUSTERING ORDER BY (deployment_id ASC, change_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
