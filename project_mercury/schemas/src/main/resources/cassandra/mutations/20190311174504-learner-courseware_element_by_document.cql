--
-- learner.courseware_element_by_document
-- --------------------
-- Tracks linked elements by competency document for the learner.
--
-- Find all the courseware elements that are linked to a competency document in a deployment history
-- > SELECT * FROM learner.courseware_element_by_document WHERE document_id = ? AND deployment_id = ?
--
CREATE TABLE IF NOT EXISTS learner.courseware_element_by_document (
    document_id uuid,
    deployment_id timeuuid,
    change_id timeuuid,
    element_id timeuuid,
    document_item_id uuid,
    element_type text,
    PRIMARY KEY ((document_id, deployment_id), change_id, element_id, document_item_id)
)
WITH CLUSTERING ORDER BY (change_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
