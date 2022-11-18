--
-- iam_global.credentials_type_by_hash
-- --------------------
-- This table stores credential info by email hash
--
-- Find credential type by email hash
-- > SELECT * FROM iam_global.credentials_type_by_hash WHERE hash = ?
--
CREATE TABLE IF NOT EXISTS iam_global.credential_type_by_hash (
    hash text,
    credential_type text,
    account_id timeuuid,
    primary key (hash, credential_type)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
