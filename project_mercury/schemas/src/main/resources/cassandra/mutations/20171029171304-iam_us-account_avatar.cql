--
-- iam_us.account_avatar
-- ---------------------
-- account_avatar for the US data region. See iam_global.account_avatar.
--
CREATE TABLE IF NOT EXISTS iam_us.account_avatar (
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
