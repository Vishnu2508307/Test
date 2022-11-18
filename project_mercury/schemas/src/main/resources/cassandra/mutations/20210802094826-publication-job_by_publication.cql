--
-- publication.job_by_publication
-- --------------------
-- This table stores mapping of jobSummary and publication
--
-- Fetch the jobSummary id with the publication id
-- > SELECT * FROM publication.job_by_publication WHERE publication_id=?
--
CREATE TABLE IF NOT EXISTS publication.job_by_publication (
    publication_id timeuuid,
    job_id timeuuid,
    PRIMARY KEY (publication_id, job_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};