--
-- learner.courseware_element_by_document_item
-- --------------------
-- Tracks all the courseware element linked to a document item
--
-- Find all the courseware element in a deployment history that are linked to a document item
-- > SELECT * FROM learner.courseware_element_by_document_item WHERE document_item_id = ? AND deployment_id = ?
--
CREATE TABLE IF NOT EXISTS learner.courseware_element_by_document_item (
    document_item_id uuid,
    deployment_id timeuuid,
    change_id timeuuid,
    element_id timeuuid,
    document_id uuid,
    element_type text,
    PRIMARY KEY ((document_item_id, deployment_id), change_id, element_id)
)
WITH CLUSTERING ORDER BY (change_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
