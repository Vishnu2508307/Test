--
-- cohort_enrollment.enrollment_status_by_cohort
-- ---------------------------------------------
-- Keep track of all the cohort enrollment events for an account over a cohort
--
-- Find the current enrollment state for an account over a cohort
-- > SELECT * FROM cohort_enrollment.enrollment_status_by_cohort WHERE cohort_id = ?
-- AND account_id = ? LIMIT 1;
--
-- Find the enrollment history for an account over a cohort
-- > SELECT * FROM cohort_enrollment.enrollment_status_by_cohort WHERE cohort_id = ?
-- AND account_id = ?;
--
CREATE TABLE IF NOT EXISTS cohort_enrollment.enrollment_status_by_cohort (
    cohort_id timeuuid,
    account_id timeuuid,
    enrollment_date timeuuid,
    enrollment_type text,
    payment_method text,
    payment_id timeuuid,
    ttl_seconds int,
    expires_at text,
    enrolled_at text,
    enrolled_by timeuuid,
    PRIMARY KEY ((cohort_id, account_id), enrollment_date)
) WITH CLUSTERING ORDER BY (enrollment_date DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
