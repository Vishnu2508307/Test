--
-- learner.cohort_by_deployment
-- --------------------
-- Tracks the cohorts using a deployment
--
-- Find all the cohorts using a deployment
-- > SELECT * FROM learner.cohort_by_deployment WHERE deployment_id = ?
--
CREATE TABLE IF NOT EXISTS learner.cohort_by_deployment (
    deployment_id timeuuid,
    cohort_id timeuuid,
    PRIMARY KEY (deployment_id, cohort_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
