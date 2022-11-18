--
-- cohort.lti_consumer_cred_v11_by_cohort
-- --------------------
-- This table stores LTI consumer key and secret
--
-- Fetch all LTI consumer key and secret by cohort id
-- > SELECT * FROM cohort.lti_consumer_cred_v11_by_cohort WHERE cohort_id = ?
--
CREATE TABLE IF NOT EXISTS cohort.lti_consumer_cred_v11_by_cohort (
    cohort_id timeuuid,
    key text,
    secret text,
    created_date bigint,
    log_debug boolean,
    PRIMARY KEY(cohort_id, key)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
