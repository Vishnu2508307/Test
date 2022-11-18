--
-- publication.publication_by_export
-- --------------------
-- This table store the publication by export.
--
-- Find the publication for given export id
-- > SELECT * FROM publication.publication_by_export WHERE export_id=?
--
CREATE TABLE IF NOT EXISTS publication.publication_by_export (
    publication_id timeuuid,
    export_id timeuuid,
    PRIMARY KEY (publication_id, export_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
