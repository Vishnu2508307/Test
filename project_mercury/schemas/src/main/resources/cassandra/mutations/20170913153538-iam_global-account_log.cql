--
-- iam_global.account_log
-- ----------------------
-- Track (for audit) actions on accounts; these rows may contain PII and should be treated accordingly.
--
-- Find all log events for an account.
-- > SELECT * FROM iam_global.account_log WHERE account_id = ?;
--
CREATE TABLE IF NOT EXISTS iam_global.account_log (
    account_id timeuuid,
    iam_region text STATIC,
    id timeuuid,
    action text,
    on_behalf_of timeuuid,
    message text,
    PRIMARY KEY(account_id, id)
)
WITH CLUSTERING ORDER BY (id DESC)
 AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
