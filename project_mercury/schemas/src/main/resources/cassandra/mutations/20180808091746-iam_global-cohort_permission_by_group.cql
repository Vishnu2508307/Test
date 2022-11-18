--
-- iam_global.cohort_permission_by_group
-- --------------------
-- Store cohort permissions by group
--
-- Find all cohort permission for a group
-- > SELECT * FROM iam_global.cohort_permission_by_group WHERE group_id = ?
--
-- Find group permission for a cohort
-- > SELECT * FROM iam_global.cohort_permission_by_group WHERE group_id = ? AND cohort_id = ?
--
CREATE TABLE IF NOT EXISTS iam_global.cohort_permission_by_group (
    group_id timeuuid,
    cohort_id timeuuid,
    permission_level text,
    PRIMARY KEY(group_id, cohort_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
