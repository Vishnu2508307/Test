--
-- competency.item
-- --------------------
-- This describes a specific learning objective or a grouping of learning objectives.
-- The table contains fields for CFItem.Type from CASE API + mercury specific fields
--
-- Find an item by id
-- > SELECT * FROM competency.item WHERE id = ?
--
CREATE TABLE IF NOT EXISTS competency.item (
    id uuid,
    document_id uuid,
    full_statement text,
    alternative_label text,
    type text,
    uri text,
    human_coding_scheme text,
    list_enumeration text,
    abbreviated_statement text,
    concept_keywords set<text>,
    concept_id uuid,
    notes text,
    language text,
    education_level set<text>,
    type_id uuid,
    license_id uuid,
    status_start_date text,
    status_end_date text,
    last_change_date_time text,
    created_at timeuuid,
    created_by timeuuid,
    modified_at timeuuid,
    modified_by timeuuid,
    PRIMARY KEY (id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
