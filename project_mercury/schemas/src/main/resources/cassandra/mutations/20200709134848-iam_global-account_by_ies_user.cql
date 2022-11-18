--
-- iam_global.account_by_ies_user
-- ------------------------------
-- Track an ies user id to its bronte account id
--
-- Find the account id for an ies user
-- > SELECT * FROM iam_global.account_by_ies_user WHERE ies_user_id = ?
--
CREATE TABLE IF NOT EXISTS iam_global.account_by_ies_user (
    ies_user_id text,
    account_id timeuuid,
    PRIMARY KEY (ies_user_id, account_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
