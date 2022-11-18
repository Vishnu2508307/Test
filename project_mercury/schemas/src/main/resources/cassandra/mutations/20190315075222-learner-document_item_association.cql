--
-- learner.document_item_association
-- --------------------
-- Store a published document item association
--
-- Find a document item association by id
-- > SELECT * FROM learner.document_item_association WHERE id = ?
--
CREATE TABLE IF NOT EXISTS learner.document_item_association (
    id uuid,
    association_grouping_id uuid,
    association_type text,
    created_at timeuuid,
    created_by timeuuid,
    destination_item_id uuid,
    document_id uuid,
    last_change_date_time text,
    origin_item_id uuid,
    sequence_number int,
    uri text,
    PRIMARY KEY (id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
