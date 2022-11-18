--
-- learner.interactive
-- --------------------
-- Stores deployed interactives
--
-- Find latest deployed interactive by id
-- > SELECT * FROM learner.interactive WHERE id = ? AND deployment_id = ? LIMIT 1
--
CREATE TABLE IF NOT EXISTS learner.interactive (
    id timeuuid,
    deployment_id timeuuid,
    change_id timeuuid,
    plugin_id timeuuid,
    plugin_version_expr text,
    config text,
    PRIMARY KEY (id, deployment_id, change_id)
)
WITH CLUSTERING ORDER BY (deployment_id ASC, change_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
