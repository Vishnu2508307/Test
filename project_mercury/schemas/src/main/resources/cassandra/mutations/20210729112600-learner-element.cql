--
-- learner.element
-- --------------------
-- Store a deployed element
--
-- Find the latest element by id and deployment
-- > SELECT * FROM learner.element WHERE id = ? AND deployment_id = ? LIMIT 1
--
CREATE TABLE IF NOT EXISTS learner.element (
    id timeuuid,
    deployment_id timeuuid,
    change_id timeuuid,
    element_type text,
    PRIMARY KEY (id, deployment_id, change_id)
)
WITH CLUSTERING ORDER BY (deployment_id ASC, change_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
