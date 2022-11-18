--
-- iam_global.document_permission_by_account
-- -----------------------------------------
-- Store document permissions by account
--
-- Find all permissions on an account
-- > SELECT * FROM iam_global.document_permission_by_account WHERE account_id =?;
--
-- Find account permissions on a document
-- > SELECT * FROM iam_global.document_permission_by_account WHERE account_id =? and document_id=?;
--
CREATE TABLE IF NOT EXISTS iam_global.document_permission_by_account (
    account_id timeuuid,
    document_id uuid,
    permission_level text,
    PRIMARY KEY(account_id, document_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
