--
-- iam_global.account_shadow_attribute
-- -----------------------------------
-- Data in this table is governed by laws and/or standards in regard to how it must handle the data.
-- Track information that we derive about accounts.
--  name   - what is being tracked, e.g. 'attending university'
--  value  - what this information is, e.g. 'Starfleet Academy'
--  source - how and when this information was gathered, e.g. 'lti' : now()
--
-- Find all the attributes for a specific account.
-- > SELECT * FROM iam_global.account_shadow_attribute WHERE account_id = ?;
--
-- Find all the values for an name on a given account.
-- > SELECT * FROM iam_global.account_shadow_attribute WHERE account_id = ? AND name = ?;
--
-- Find a specific value on a given name and account.
-- > SELECT * FROM iam_global.account_shadow_attribute WHERE account_id = ? AND name = ? AND value = ?;
--
CREATE TABLE IF NOT EXISTS iam_global.account_shadow_attribute (
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
