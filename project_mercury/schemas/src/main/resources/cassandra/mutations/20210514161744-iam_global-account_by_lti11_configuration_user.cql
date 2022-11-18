--
-- iam_global.account_by_lti11_configuration_user
-- ----------------------------------------------
-- Store the corresponding bronte account id for a user id on a global lti configuration
--
-- Find the bronte account id for a global configuration where the user is
-- > SELECT * FROM iam_global.account_by_lti11_configuration_user WHERE consumer_configuration_id = ?
-- AND user_id = ?
--
CREATE TABLE IF NOT EXISTS iam_global.account_by_lti11_configuration_user (
    consumer_configuration_id timeuuid,
    user_id text,
    account_id timeuuid,
    PRIMARY KEY (consumer_configuration_id, user_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
