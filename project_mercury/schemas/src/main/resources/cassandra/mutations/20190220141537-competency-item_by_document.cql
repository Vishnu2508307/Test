--
-- competency.item_by_document
-- --------------------
-- Tracks competency items for a document.
--
-- Find all items by a document
-- > SELECT * FROM competency.item_by_document WHERE document_id = ?
--
CREATE TABLE IF NOT EXISTS competency.item_by_document (
    document_id uuid,
    item_id uuid,
    PRIMARY KEY (document_id, item_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
