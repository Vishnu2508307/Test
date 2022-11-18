--
-- learner.document_item_by_courseware_element
-- --------------------
-- Tracks all document items that have been linked to a courseware element
--
-- Find all the document items linked to a courseware element in a given deployment history
-- > SELECT * FROM learner.document_item_by_courseware_element WHERE element_id = ? AND deployment_id = ?
--
CREATE TABLE IF NOT EXISTS learner.document_item_by_courseware_element (
    element_id timeuuid,
    deployment_id timeuuid,
    change_id timeuuid,
    document_item_id uuid,
    document_id uuid,
    element_type text,
    PRIMARY KEY ((element_id, deployment_id), change_id, document_item_id)
)
WITH CLUSTERING ORDER BY (change_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
