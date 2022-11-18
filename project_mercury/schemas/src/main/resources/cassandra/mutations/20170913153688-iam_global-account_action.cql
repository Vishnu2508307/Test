--
-- iam_global.account_action
-- -------------------------
-- Track high-level events/actions that occured on an account, such as the user endorsed a lesson.
--
-- Find all actions for an account
-- > SELECT * FROM iam_global.account_action WHERE account_id = ?;
--
-- Find all actions for an account and action
-- > SELECT * FROM iam_global.account_action WHERE account_id = ? AND action = ?;
--
CREATE TABLE IF NOT EXISTS iam_global.account_action (
    account_id timeuuid,
    action text,
    id timeuuid,
    PRIMARY KEY(account_id, action)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
