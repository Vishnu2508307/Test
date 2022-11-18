--
-- learner.pathway
-- --------------------
-- Store deployed pathways along with their deployment and change ids
--
-- Find the latest change id for a deployed pathway
-- > SELECT * FROM learner.pathway WHERE id = ? AND deployment_id = ? LIMIT 1
--
CREATE TABLE IF NOT EXISTS learner.pathway (
    id timeuuid,
    deployment_id timeuuid,
    change_id timeuuid,
    type text,
    PRIMARY KEY (id, deployment_id, change_id)
)
WITH CLUSTERING ORDER BY (deployment_id ASC, change_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
