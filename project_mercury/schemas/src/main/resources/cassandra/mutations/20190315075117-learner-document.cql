--
-- learner.document
-- --------------------
-- Store a published static competency document
--
-- Find a published document by id
-- > SELECT * FROM learner.document WHERE id = ?
--
CREATE TABLE IF NOT EXISTS learner.document (
    id uuid,
    adoption_status text,
    created_at timeuuid,
    created_by timeuuid,
    creator text,
    description text,
    language text,
    last_change_date_time text,
    license_id uuid,
    modified_at timeuuid,
    modified_by timeuuid,
    notes text,
    official_source_url text,
    origin text,
    publisher text,
    status_end_date text,
    status_start_date text,
    subject set<text>,
    subject_ids set<uuid>,
    title text,
    uri text,
    version text,
    document_version_id timeuuid,
    published_by timeuuid,
    PRIMARY KEY (id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
