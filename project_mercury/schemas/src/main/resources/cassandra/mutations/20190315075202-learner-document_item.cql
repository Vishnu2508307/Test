--
-- learner.document_item
-- --------------------
-- Store a published static document item
--
-- Find a published document item for by id
-- > SELECT * FROM learner.document_item WHERE id = ?
--
CREATE TABLE IF NOT EXISTS learner.document_item (
    id uuid,
    abbreviated_statement text,
    alternative_label text,
    concept_id uuid,
    concept_keywords set<text>,
    created_at timeuuid,
    created_by timeuuid,
    document_id uuid,
    education_level set<text>,
    full_statement text,
    human_coding_scheme text,
    language text,
    last_change_date_time text,
    license_id uuid,
    list_enumeration text,
    modified_at timeuuid,
    modified_by timeuuid,
    notes text,
    status_end_date text,
    status_start_date text,
    type text,
    type_id uuid,
    uri text,
    PRIMARY KEY (id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
