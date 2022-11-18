--
-- learner.document_item_association_by_origin
-- --------------------
-- Store all the published document item association retrivable by origin item id
--
-- Find all the associations for a given origin item id
-- > SELECT * FROM learner.document_item_association_by_origin WHERE origin_item_id = ?
--
-- Find all the associations for a given origin item id with a specific association type
-- > SELECT * FROM learner.document_item_association_by_origin WHERE origin_item_id = ? and association_type = ?
--
CREATE TABLE IF NOT EXISTS learner.document_item_association_by_origin (
    origin_item_id uuid,
    association_type text,
    association_id uuid,
    destination_item_id uuid,
    PRIMARY KEY (origin_item_id, association_type, association_id)
)
WITH CLUSTERING ORDER BY (association_type ASC, association_id ASC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
