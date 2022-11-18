--
-- learner.deployment_log_by_deployment
-- --------------------
-- Store deployment steps logs in a descending order.
--
-- Find the last log for a deployment
-- > SELECT * FROM learner.deployment_log_by_deployment WHERE cohort_id = ? AND deployment_id = ? AND change_id = ? LIMIT 1
--
-- Find all the logs for a deployment
-- > SELECT * FROM learner.deployment_log_by_deployment WHERE cohort_id = ? AND deployment_id = ? AND change_id = ?
--
CREATE TABLE IF NOT EXISTS learner.deployment_log_by_deployment (
    cohort_id timeuuid,
    deployment_id timeuuid,
    change_id timeuuid,
    id timeuuid,
    state text,
    message text,
    element_id timeuuid,
    element_type text,
    PRIMARY KEY ((cohort_id, deployment_id, change_id), id)
) WITH CLUSTERING ORDER BY (id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
