--
-- iam_global.account_identity_attribute
-- -------------------------------------
-- Identity attributes of an account; contains ONLY personally identifiable information (PII).
-- Data in this table is governed by laws and/or standards in regard to how it must handle the data.
--
-- Fetch account identity attributes by id
-- > SELECT * FROM iam_global.account_identity_attribute WHERE id = ?;
--
CREATE TABLE IF NOT EXISTS iam_global.account_identity_attribute (
    account_id timeuuid,
    iam_region text,
    subscription_id timeuuid,
    given_name text,
    family_name text,
    honorific_prefix text,
    honorific_suffix text,
    email set<text>,
    primary_email text,
    affiliation text,
    job_title text,
    PRIMARY KEY (account_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
