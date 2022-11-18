--
-- learner.activity_by_deployment
-- --------------------
-- Store a deployed activity
--
-- Find the latest activity by id and deployment
-- > SELECT * FROM learner.activity_by_deployment WHERE id = ? AND deployment_id = ? LIMIT 1
--
CREATE TABLE IF NOT EXISTS learner.activity_by_deployment (
    id timeuuid,
    deployment_id timeuuid,
    change_id timeuuid,
    plugin_id timeuuid,
    plugin_version_expr text,
    config text,
    theme text,
    creator_id timeuuid,
    student_scope_urn timeuuid,
    evaluation_mode text,
    PRIMARY KEY ((id, deployment_id), change_id)
)
WITH CLUSTERING ORDER BY (change_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
