--
-- iam_global.account_avatar
-- -------------------------
-- Table to record the avatar/photo of the account. The data values are base64 encoded. This table contains PII.
--
-- Find all the avatars for a given account
-- > SELECT * FROM iam_global.account_avatar WHERE account_id = ?
--
-- Find a specific avatar for an account, e.g. original
-- > SELECT * FROM iam_global.account_avatar WHERE account_id = ? AND name = ?
--
CREATE TABLE IF NOT EXISTS iam_global.account_avatar (
    account_id timeuuid,
    iam_region text STATIC,
    name text,
    mime_type text,
    meta map<text, text>,
    data text,
    PRIMARY KEY (account_id, name)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
