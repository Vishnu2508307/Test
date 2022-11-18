--
-- learner.document_item_association_by_document
-- --------------------
-- Store published document item association ids for a document.
--
-- Find all the existing association ids for a document
-- > SELECT * FROM learner.document_item_association_by_document WHERE document_id = ?
--
CREATE TABLE IF NOT EXISTS learner.document_item_association_by_document (
    document_id uuid,
    association_id uuid,
    PRIMARY KEY (document_id, association_id)
)
WITH CLUSTERING ORDER BY (association_id ASC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
