--
-- learner.scenario
-- --------------------
-- Stores deployed scenarios
--
-- Find the latest deployed scenario for a deployment
-- > SELECT * FROM learner.scenario WHERE id = ? AND deployment_id = ? LIMIT 1
--
CREATE TABLE IF NOT EXISTS learner.scenario (
    id timeuuid,
    deployment_id timeuuid,
    change_id timeuuid,
    actions text,
    condition text,
    correctness text,
    lifecycle text,
    name text,
    description text,
    PRIMARY KEY (id, deployment_id, change_id)
)
WITH CLUSTERING ORDER BY (deployment_id ASC, change_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
