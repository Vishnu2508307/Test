--
-- learner.document_item_by_document
-- --------------------
-- Store all the document items that belongs to a document.
--
-- Find all the document items that belongs to a given document
-- > SELECT * FROM learner.document_item_by_document WHERE document_id = ?
--
CREATE TABLE IF NOT EXISTS learner.document_item_by_document (
    document_id uuid,
    document_item_id uuid,
    PRIMARY KEY (document_id, document_item_id)
)
WITH CLUSTERING ORDER BY (document_item_id ASC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
