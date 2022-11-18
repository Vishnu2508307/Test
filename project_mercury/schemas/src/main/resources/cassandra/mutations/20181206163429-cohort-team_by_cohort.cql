--
-- cohort.team_by_cohort
-- --------------------
-- Store all the teams collaborating to a cohort.
--
-- Find all team that are collaborating to a cohort
-- > SELECT * FROM cohort.team_by_cohort WHERE cohort_id = ?
--
CREATE TABLE IF NOT EXISTS cohort.team_by_cohort (
    cohort_id timeuuid,
    team_id timeuuid,
    permission_level text,
    PRIMARY KEY(cohort_id, team_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
