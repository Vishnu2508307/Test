--
-- publication.artifact
-- --------------------
-- This table stores artifact information.
--
-- Fetch the artifact info with its id
-- > SELECT * FROM publication.artifact WHERE id=?
--
CREATE TABLE IF NOT EXISTS publication.artifact (
    id timeuuid,
    job_id timeuuid,
    artifact_type text,
    PRIMARY KEY (job_id, id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
