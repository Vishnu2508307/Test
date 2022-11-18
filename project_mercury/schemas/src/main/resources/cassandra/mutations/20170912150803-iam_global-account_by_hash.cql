--
-- iam_global.account_by_hash
-- --------------------------
-- Locate an account by hash (e.g. SHA256 of lowercase email)
--
-- Find an account by email hash.
-- > SELECT * FROM iam_global.account_by_hash WHERE hash = ?;
--
CREATE TABLE IF NOT EXISTS iam_global.account_by_hash (
    hash text,
    account_id timeuuid,
    iam_region text,
    PRIMARY KEY (hash)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
