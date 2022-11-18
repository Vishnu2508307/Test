--
-- iam_global.cohort_permission_by_account
-- --------------------
-- Store cohort permissions by account
--
-- Find all permissions for an account
-- > SELECT * FROM iam_global.cohort_permission_by_account WHERE account_id = ?
--
-- Find account permission on a cohort
-- > SELECT * FROM iam_global.cohort_permission_by_account WHERE account_id = ? AND cohort_id = ?
--
CREATE TABLE IF NOT EXISTS iam_global.cohort_permission_by_account (
    account_id timeuuid,
    cohort_id timeuuid,
    permission_level text,
    PRIMARY KEY(account_id, cohort_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
