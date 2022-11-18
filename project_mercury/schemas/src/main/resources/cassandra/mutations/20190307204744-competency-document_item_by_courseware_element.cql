--
-- competency.document_item_by_courseware_element
-- --------------------
-- Tracks all the document items that are linked to a courseware element
--
-- Find all the document items for a coureware element
-- > SELECT * FROM competency.document_item_by_courseware_element WHERE courseware_element = ?
--
CREATE TABLE IF NOT EXISTS competency.document_item_by_courseware_element (
    element_id timeuuid,
    document_item_id uuid,
    document_id uuid,
    element_type text,
    PRIMARY KEY (element_id, document_item_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
