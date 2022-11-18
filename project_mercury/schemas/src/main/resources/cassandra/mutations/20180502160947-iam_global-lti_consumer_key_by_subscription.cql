--
-- iam.lti_consumer_key_by_subscription
-- --------------------
-- Tracks Lti Consumers keys by subscription Id
--
-- Find all Lti Consumer Key for given subscription id
-- > SELECT * FROM iam_global.lti_consumer_key_by_subscription WHERE subscription_id = ?
--
CREATE TABLE IF NOT EXISTS iam_global.lti_consumer_key_by_subscription (
    subscription_id timeuuid,
    key text,
    secret text,
    PRIMARY KEY (subscription_id, key)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
