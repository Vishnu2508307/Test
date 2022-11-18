--
-- cohort.account_by_cohort
-- --------------------
-- Store the collaborators for a cohort
--
-- Find all collaborators for a cohort
-- > SELECT * FROM cohort.account_by_cohort WHERE cohort_id = ?
--
CREATE TABLE IF NOT EXISTS cohort.account_by_cohort (
    cohort_id timeuuid,
    account_id timeuuid,
    permission_level text,
    PRIMARY KEY(cohort_id, account_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
