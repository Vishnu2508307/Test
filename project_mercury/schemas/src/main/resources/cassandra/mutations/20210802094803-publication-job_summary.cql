--
-- publication.job_summary
-- --------------------
-- This table stores jobSummary information.
--
-- Fetch jobSummary information with its id
-- > SELECT * FROM publication.job_summary WHERE id=?
--
CREATE TABLE IF NOT EXISTS publication.job_summary (
    id timeuuid,
    job_type text,
    status text,
    PRIMARY KEY (id, job_type)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};