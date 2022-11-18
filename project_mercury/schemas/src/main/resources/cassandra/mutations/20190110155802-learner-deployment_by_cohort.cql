--
-- learner.deployment_by_cohort
-- --------------------
-- Tracks the deployments for a cohort
--
-- Find all the deployments in a cohort
-- > SELECT * FROM learner.deployment_by_cohort WHERE cohort_id = ?
--
CREATE TABLE IF NOT EXISTS learner.deployment_by_cohort (
    cohort_id timeuuid,
    deployment_id timeuuid,
    PRIMARY KEY (cohort_id, deployment_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
