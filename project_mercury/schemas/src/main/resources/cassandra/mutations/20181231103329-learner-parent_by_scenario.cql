--
-- learner.parent_by_scenario
-- --------------------
-- Allows fetching a parent element of a given scenario
--
-- Find the parent element for a scenario based on the latest change id
-- > SELECT * FROM learner.parent_by_scenario WHERE scenario_id = ? AND deployment_id = ? LIMIT 1
--
CREATE TABLE IF NOT EXISTS learner.parent_by_scenario (
    scenario_id timeuuid,
    deployment_id timeuuid,
    change_id timeuuid,
    parent_id timeuuid,
    parent_type text,
    PRIMARY KEY (scenario_id, deployment_id, change_id)
)
WITH CLUSTERING ORDER BY (deployment_id ASC, change_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
