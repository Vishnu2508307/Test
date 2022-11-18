--
-- competency.item_association_by_destination
-- --------------------
-- Tracks item associations by destination node
--
-- Find all associations where item is destination
-- > SELECT * FROM competency.item_association_by_destination WHERE destination_item_id = ?
--
-- Find all associations where item is destination and association type is 'isChildOf' (find children for an item)
-- > SELECT * FROM competency.item_association_by_destination WHERE destination_item_id = ? and association_type = 'isChildOf'
--
CREATE TABLE IF NOT EXISTS competency.item_association_by_destination (
    destination_item_id uuid,
    association_type text,
    association_id uuid,
    origin_item_id uuid,
    PRIMARY KEY(destination_item_id, association_type, association_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
