--
-- cohort_enrollment.enrollment_by_cohort
-- ---------------------------------------
-- Store all the accounts enrolled in a cohort. 
-- The table uses TTL, e.g. to remove FREE_TRIAL enrollments when the trial period expires.
--
-- Find all accounts enrolled in a cohort
-- > SELECT * FROM cohort_enrollment.enrollment_by_cohort WHERE cohort_id = ?
--
CREATE TABLE IF NOT EXISTS cohort_enrollment.enrollment_by_cohort (
    cohort_id timeuuid,
    account_id timeuuid,
    enrollment_date timeuuid,
    enrollment_type text,
    PRIMARY KEY(cohort_id, account_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
