--
-- competency.item_association
-- --------------------
-- This is data about the relationship between two competency items
-- The table contains fields for CFAssociation.Type from CASE API + mercury specific fields.
--
-- Find an item association by id
-- > SELECT * FROM competency.item_association WHERE id = ?
--
CREATE TABLE IF NOT EXISTS competency.item_association (
    id uuid,
    document_id uuid,
    association_type text,
    sequence_number int,
    uri text,
    origin_item_id uuid,
    destination_item_id uuid,
    association_grouping_id uuid,
    last_change_date_time text,
    created_at timeuuid,
    created_by timeuuid,
    PRIMARY KEY (id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
