--
-- publication.publication_by_export
-- --------------------
-- This table store the publication by export.
--
-- Find the publication for given export id
-- > SELECT * FROM publication.publication_by_export WHERE export_id=?
--
CREATE TABLE IF NOT EXISTS publication.publication_by_export (
    export_id timeuuid,
    publication_id timeuuid,
    PRIMARY KEY (export_id, publication_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
