--
-- iam_global.cohort_permission_by_team
-- --------------------
-- Store cohort permissions by team
--
-- Find all cohort permission for a team
-- > SELECT * FROM iam_global.cohort_permission_by_team WHERE team_id = ?
--
-- Find team permission for a cohort
-- > SELECT * FROM iam_global.cohort_permission_by_team WHERE team_id = ? AND cohort_id = ?
--
CREATE TABLE IF NOT EXISTS iam_global.cohort_permission_by_team (
    team_id timeuuid,
    cohort_id timeuuid,
    permission_level text,
    PRIMARY KEY(team_id, cohort_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
