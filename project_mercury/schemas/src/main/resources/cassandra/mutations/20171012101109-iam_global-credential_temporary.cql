--
-- iam_global.credential_temporary
-- -------------------------------
-- Storage of temporary credential tokens, used in such flows as password resets and validation.
-- Generally these credentials are stored with a time to live (TTL) based on usage/type.
--
-- Find a specific authorization code
-- > SELECT * FROM iam_global.credential_temporary WHERE authorization_code = ?;
--
CREATE TABLE IF NOT EXISTS iam_global.credential_temporary (
    authorization_code text,
    type text,
    account_id timeuuid,
    PRIMARY KEY (authorization_code)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
