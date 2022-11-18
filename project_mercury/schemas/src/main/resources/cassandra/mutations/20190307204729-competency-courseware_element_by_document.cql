--
-- competency.courseware_element_by_document
-- --------------------
-- Tracks all the courseware elements that are tagged with document items from a specific document
--
-- Find all courseware element that are tagged with items from a specific document
-- > SELECT * FROM competency.courseware_element_by_document WHERE document_id = ?
--
CREATE TABLE IF NOT EXISTS competency.courseware_element_by_document (
    document_id uuid,
    element_id timeuuid,
    document_item_id uuid,
    element_type text,
    PRIMARY KEY (document_id, element_id, document_item_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
