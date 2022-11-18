--
-- learner.parent_by_component
-- --------------------
-- Store the relationship between a deployed component and its deployed parent element
--
-- Find the latest deployed parent for a component
-- > SELECT * FROM learner.parent_by_component WHERE component_id = ? AND deployment_id = ? LIMIT 1
--
CREATE TABLE IF NOT EXISTS learner.parent_by_component (
    component_id timeuuid,
    deployment_id timeuuid,
    change_id timeuuid,
    parent_id timeuuid,
    parent_type text,
    PRIMARY KEY (component_id, deployment_id, change_id)
)
WITH CLUSTERING ORDER BY (deployment_id ASC, change_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
