--
-- iam_global.account
-- ------------------
-- Main tracking for an account; MUST NOT contain personally identifiable information (PII)
--
-- TODO: password_hash and password_expired here temporarily until credentials are implemented.
--
-- Fetch an account by id.
-- > SELECT * FROM iam_global.account WHERE id = ?;
--
CREATE TABLE IF NOT EXISTS iam_global.account (
    id timeuuid,
    historical_id bigint,
    subscription_id timeuuid,
    iam_region text,
    status text,
    roles set<text>,
    password_hash text,
    password_expired boolean,
    password_temporary text,
    PRIMARY KEY (id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};

