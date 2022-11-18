--
-- iam_au.account_shadow_attribute
-- -------------------------------
-- account_shadow_attribute for the AU data region. See iam_global.account_shadow_attribute.
--
CREATE TABLE IF NOT EXISTS iam_au.account_shadow_attribute (
    account_id timeuuid,
    iam_region text STATIC,
    name text,
    value text,
    source map<timeuuid, text>,
    PRIMARY KEY (account_id, name, value)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
