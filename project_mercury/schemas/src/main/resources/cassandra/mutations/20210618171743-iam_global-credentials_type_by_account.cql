--
-- iam_global.credentials_type_by_account
-- --------------------
-- This table stores credential info by account
--
-- Find credentials type by account
-- > SELECT * FROM iam_global.credentials_type_by_account WHERE account_id = ?
--
CREATE TABLE IF NOT EXISTS iam_global.credentials_type_by_account (
    credential_type text,
    account_id timeuuid,
    hash text,
    primary key (account_id, credential_type)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
