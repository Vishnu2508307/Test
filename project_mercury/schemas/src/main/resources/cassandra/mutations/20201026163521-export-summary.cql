--
-- export.summary
-- --------------------
-- This table store the export summary details
--
-- Find the export summary details for given id
-- > SELECT * FROM export.summary WHERE id=?
--
CREATE TABLE IF NOT EXISTS export.summary (
    id timeuuid,
    element_id timeuuid,
    account_id timeuuid,
    workspace_id timeuuid,
    element_type text,
    status text,
    project_id timeuuid,
    completed_at timeuuid,
    primary key (id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
