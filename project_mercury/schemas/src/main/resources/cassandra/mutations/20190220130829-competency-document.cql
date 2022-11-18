--
-- competency.document
-- --------------------
-- This is a root for learning objectives.
-- The table contains fields for CFDocument.Type from CASE API + mercury specific fields
--
-- Find document by id
-- > SELECT * FROM competency.document WHERE id = ?
--
CREATE TABLE IF NOT EXISTS competency.document (
    id uuid,
    uri text,
    creator text,
    title text,
    last_change_date_time text,
    official_source_url text,
    publisher text,
    description text,
    subject set<text>,
    subject_ids set<uuid>,
    language text,
    version text,
    adoption_status text,
    status_start_date text,
    status_end_date text,
    license_id uuid,
    notes text,
    created_at timeuuid,
    created_by timeuuid,
    modified_at timeuuid,
    modified_by timeuuid,
    origin text,
    workspace_id timeuuid,
    PRIMARY KEY (id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
