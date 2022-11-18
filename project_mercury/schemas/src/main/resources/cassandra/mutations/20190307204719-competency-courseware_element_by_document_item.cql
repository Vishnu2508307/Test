--
-- competency.courseware_element_by_document_item
-- --------------------
-- Traks all the courseware elements linked to a document item
--
-- Find all the courseware elements that are tagged with a document item
-- > SELECT * FROM competency.courseware_element_by_document_item WHERE document_item_id = ?;
--
CREATE TABLE IF NOT EXISTS competency.courseware_element_by_document_item (
    document_item_id uuid,
    element_id timeuuid,
    element_type text,
    document_id uuid,
    PRIMARY KEY (document_item_id, element_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
