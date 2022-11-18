--
-- cohort.cohort_by_team
-- --------------------
-- Store the cohorts a team has access to
--
-- Find all cohorts a team has access to
-- > SELECT * FROM cohort.cohort_by_team WHERE team_id = ?
--
CREATE TABLE IF NOT EXISTS cohort.cohort_by_team (
    team_id timeuuid,
    cohort_id timeuuid,
    PRIMARY KEY(team_id, cohort_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
