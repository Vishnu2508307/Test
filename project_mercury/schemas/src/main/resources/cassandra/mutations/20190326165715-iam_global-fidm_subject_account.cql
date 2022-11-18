--
-- iam_global.fidm_subject_account
-- -------------------------------
-- Keeps linking between our account and federated identity
--
-- Find an IAM account by federated identity
-- > SELECT * FROM iam_global.fidm_subject_account WHERE subscription_id = ? and client_id = ? and subject_id = ?
--
CREATE TABLE IF NOT EXISTS iam_global.fidm_subject_account (
    subscription_id timeuuid,
    client_id text,
    subject_id text,
    account_id timeuuid,
    PRIMARY KEY (subscription_id, client_id, subject_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
