--
-- learner.deployment_log_by_state
-- --------------------
-- Store deployment steps logs by state in a descending time order.
--
-- Find all the logs for a deployment with a specific state
-- > SELECT * FROM learner.deployment_log_by_state WHERE cohort_id = ? AND deployment_id = ? AND change_id = ? AND state = ?
--
-- Find the last log for a deployment with a specific state
-- > SELECT * FROM learner.deployment_log_by_state WHERE cohort_id = ? AND deployment_id = ? AND change_id = ? AND state = ? LIMIT 1
--
CREATE TABLE IF NOT EXISTS learner.deployment_log_by_state (
    cohort_id timeuuid,
    deployment_id timeuuid,
    change_id timeuuid,
    state text,
    id timeuuid,
    message text,
    element_id timeuuid,
    element_type text,
    PRIMARY KEY ((cohort_id, deployment_id, change_id, state), id)
) WITH CLUSTERING ORDER BY (id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
