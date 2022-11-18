--
-- iam.lti_consumer_key
-- --------------------
-- Keeps all LTI Consumers keys
--
-- Fetch Lti Consumer by key
-- > SELECT * FROM iam_global.lti_consumer_key WHERE key=?
--
CREATE TABLE IF NOT EXISTS iam_global.lti_consumer_key (
    key text,
    secret text,
    subscription_id timeuuid,
    PRIMARY KEY (key)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
