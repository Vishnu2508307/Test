--
-- competency.document_by_account
-- ------------------------
-- Stores the documents an account has access to.
--
-- Find all documents an account has access to.
-- > SELECT * FROM competency.document_by_account WHERE account_id = ?;
--
CREATE TABLE IF NOT EXISTS competency.document_by_account (
    account_id timeuuid,
    document_id uuid,
    PRIMARY KEY(account_id,document_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
