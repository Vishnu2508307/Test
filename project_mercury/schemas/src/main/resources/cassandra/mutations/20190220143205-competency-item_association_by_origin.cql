--
-- competency.item_association_by_origin
-- --------------------
-- Tracks item associations by origin node
--
-- Find all associations where item is origin
-- > SELECT * FROM competency.item_association_by_origin WHERE origin_item_id = ?
--
-- Find all associations where item is origin and association type is 'isChildOf' (find parents for an item)
-- > SELECT * FROM competency.item_association_by_origin WHERE origin_item_id = ? and association_type = 'isChildOf'
--
CREATE TABLE IF NOT EXISTS competency.item_association_by_origin (
    origin_item_id uuid,
    association_type text,
    association_id uuid,
    destination_item_id uuid,
    PRIMARY KEY (origin_item_id, association_type, association_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
