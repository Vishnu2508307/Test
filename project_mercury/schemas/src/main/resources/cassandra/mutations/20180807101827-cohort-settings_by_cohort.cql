--
-- cohort.settings_by_cohort
-- --------------------
-- Store the settings for a cohort.
--
-- Find settings by cohort id
-- > SELECT * FROM cohort.settings_by_cohort WHERE cohort_id = ?
--
CREATE TABLE IF NOT EXISTS cohort.settings_by_cohort (
    cohort_id timeuuid,
    banner_pattern text,
    color text,
    PRIMARY KEY (cohort_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
