--
-- competency.document_version
-- --------------------
-- Keeps mark when document has been changed
--
-- Find the latest version_id for a document
-- > SELECT * FROM competency.document_version WHERE document_id = ? limit 1
--
CREATE TABLE IF NOT EXISTS competency.document_version (
    document_id uuid,
    version_id timeuuid,
    author_id timeuuid,
    PRIMARY KEY(document_id, version_id)
) WITH CLUSTERING ORDER BY (version_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
