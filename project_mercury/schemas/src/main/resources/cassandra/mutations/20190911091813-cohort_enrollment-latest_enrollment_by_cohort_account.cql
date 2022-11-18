--
-- cohort_enrollment.latest_enrollment_by_cohort_account
-- -----------------------------------------------------
-- Track the last enrollment state for every account that was ever enrolled to the cohort.
--
-- Find all the accounts that were ever enrolled to a cohort
-- > SELECT * FROM cohort_enrollment.latest_enrollment_by_cohort_account WHERE cohort_id = ?
--
CREATE TABLE IF NOT EXISTS cohort_enrollment.latest_enrollment_by_cohort_account (
    cohort_id timeuuid,
    account_id timeuuid,
    enrolled_at text,
    enrolled_by timeuuid,
    enrollment_date timeuuid,
    enrollment_type text,
    expires_at text,
    payment_id timeuuid,
    payment_method text,
    ttl_seconds int,
    PRIMARY KEY (cohort_id, account_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
