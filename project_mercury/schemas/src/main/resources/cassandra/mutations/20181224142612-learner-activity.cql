--
-- learner.activity
-- --------------------
-- Store a deployed activity
--
-- Find the latest activity by id and deployment
-- > SELECT * FROM learner.activity WHERE id = ? AND deployment_id = ? LIMIT 1
--
CREATE TABLE IF NOT EXISTS learner.activity (
    id timeuuid,
    deployment_id timeuuid,
    change_id timeuuid,
    plugin_id timeuuid,
    plugin_version_expr text,
    config text,
    theme text,
    creator_id timeuuid,
    PRIMARY KEY (id, deployment_id, change_id)
)
WITH CLUSTERING ORDER BY (deployment_id ASC, change_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
