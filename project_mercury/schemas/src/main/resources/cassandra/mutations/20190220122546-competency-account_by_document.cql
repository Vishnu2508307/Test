--
-- competency.account_by_document
-- ------------------------
-- Store collaborators for a document
--
-- Find all collaborators for a document
-- > SELECT * FROM competency.account_by_document WHERE document_id = ?;
--
CREATE TABLE IF NOT EXISTS competency.account_by_document (
    document_id uuid,
    account_id timeuuid,
    permission_level text,
    PRIMARY KEY(document_id, account_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
