--
-- iam_us.account_identity_attribute
-- ---------------------------------
-- account_identity_attribute for the US data region. See iam_global.account_identity_attribute.
--
CREATE TABLE IF NOT EXISTS iam_us.account_identity_attribute (
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
