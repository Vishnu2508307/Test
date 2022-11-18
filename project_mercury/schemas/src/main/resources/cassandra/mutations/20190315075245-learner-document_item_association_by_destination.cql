--
-- learner.document_item_association_by_destination
-- --------------------
-- Store published item associations retrivable by destination item id.
--
-- Find all association for a destination item
-- > SELECT * FROM learner.document_item_association_by_destination WHERE destination_item_id = ?
--
-- Find all association for a destination item with a specific association type
-- > SELECT * FROM learner.document_item_association_by_destination WHERE destination_item_id = ? AND association_type = ?
--
CREATE TABLE IF NOT EXISTS learner.document_item_association_by_destination (
    destination_item_id uuid,
    association_type text,
    association_id uuid,
    origin_item_id uuid,
    PRIMARY KEY (destination_item_id, association_type, association_id)
)
WITH CLUSTERING ORDER BY (association_type ASC, association_id ASC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
