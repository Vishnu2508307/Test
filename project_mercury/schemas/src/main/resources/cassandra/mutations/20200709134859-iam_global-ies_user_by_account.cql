--
-- iam_global.ies_user_by_account
-- ------------------------------
-- Track an account if in bronte for a ies user id
--
-- Find the ies user id for an account id
-- > SELECT * FROM iam_global.ies_user_by_account WHERE account_id = ?
--
CREATE TABLE IF NOT EXISTS iam_global.ies_user_by_account (
    account_id timeuuid,
    ies_user_id text,
    PRIMARY KEY (account_id, ies_user_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
