--
-- cohort.summary
-- --------------------
-- Store the cohort entity summary information.
--
-- Find a cohort by id
-- > SELECT * FROM cohort.summary WHERE id = ?
--
CREATE TABLE IF NOT EXISTS cohort.summary (
    id timeuuid,
    name text,
    type text,
    start_date bigint,
    end_date bigint,
    finished_date timeuuid,
    workspace_id timeuuid,
    creator_id timeuuid,
    PRIMARY KEY (id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};