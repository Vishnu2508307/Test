--
-- cohort.cohort_by_account
-- --------------------
-- Store the cohorts an account has access to
--
-- Find all cohorts an account has access to
-- > SELECT * FROM cohort.cohort_by_account WHERE account_id = ?
--
CREATE TABLE IF NOT EXISTS cohort.cohort_by_account (
    account_id timeuuid,
    cohort_id timeuuid,
    PRIMARY KEY (account_id, cohort_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
