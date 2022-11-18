--
-- competency.document_by_workspace
-- --------------------
-- Tracks documents by workspace
--
-- find all documents in a workspace
-- > SELECT * FROM competency.document_by_workspace WHERE workspace_id = ?
--
CREATE TABLE IF NOT EXISTS competency.document_by_workspace (
    workspace_id timeuuid,
    document_id uuid,
    PRIMARY KEY (workspace_id, document_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
