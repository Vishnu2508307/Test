--
-- iam_us.account_log
-- ------------------
-- account_log for the US data region. See iam_global.account_log.
--
CREATE TABLE IF NOT EXISTS iam_us.account_log (
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
