--
-- publication.artifact_content
-- --------------------
-- This table stores artifact content details.
--
-- Fetch artifact content detail with it id
-- > SELECT * FROM publication.artifact_content WHERE id=?
--
CREATE TABLE IF NOT EXISTS publication.artifact_content (
    id timeuuid,
    artifact_id timeuuid,
    PRIMARY KEY (artifact_id, id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};