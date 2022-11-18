--
-- cohort_enrollment.enrollment_by_account
-- ---------------------------------------
-- Store all the cohorts enrollment for an account. 
-- The table uses TTL, e.g. to remove FREE_TRIAL enrollments when the trial period expires.
--
-- Find all cohorts a user is enrolled in
-- > SELECT * FROM cohort_enrollment.enrollment_by_account WHERE account_id = ?
--
CREATE TABLE IF NOT EXISTS cohort_enrollment.enrollment_by_account (
    account_id timeuuid,
    cohort_id timeuuid,
    enrollment_date timeuuid,
    enrollment_type text,
    PRIMARY KEY(account_id, cohort_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
