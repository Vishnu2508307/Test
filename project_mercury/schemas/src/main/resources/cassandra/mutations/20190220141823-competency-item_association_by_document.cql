--
-- competency.item_association_by_document
-- --------------------
-- Tracks item association for a document.
--
-- Find all item associations by a document
-- > SELECT * FROM competency.item_association_by_document WHERE document_id = ?
--
CREATE TABLE IF NOT EXISTS competency.item_association_by_document (
    document_id uuid,
    association_id uuid,
    PRIMARY KEY (document_id, association_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
