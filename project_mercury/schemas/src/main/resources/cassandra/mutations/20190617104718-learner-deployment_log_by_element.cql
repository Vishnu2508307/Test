--
-- learner.deployment_log_by_element
-- --------------------
-- Store deployment steps logs by element in a descending time order.
--
-- Find all the logs for a deployment on a specific courseware element
-- > SELECT * FROM learner.deployment_log_by_element WHERE cohort_id = ? AND deployment_id = ? AND change_id = ? AND element_id = ?
--
-- Find the last log for a deployment on a specific courseware element
-- > SELECT * FROM learner.deployment_log_by_element WHERE cohort_id = ? AND deployment_id = ? AND change_id = ? AND element_id = ? LIMIT 1
--
CREATE TABLE IF NOT EXISTS learner.deployment_log_by_element (
    cohort_id timeuuid,
    deployment_id timeuuid,
    change_id timeuuid,
    element_id timeuuid,
    id timeuuid,
    state text,
    message text,
    element_type text,
    PRIMARY KEY ((cohort_id, deployment_id, change_id, element_id), id)
) WITH CLUSTERING ORDER BY (id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
